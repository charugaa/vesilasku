package fi.kapsi.kosmik.vesilasku

import java.time.{Month, YearMonth}

import fi.kapsi.kosmik.vesilasku.csv.toCsv
import fi.kapsi.kosmik.vesilasku.model._
import fi.kapsi.kosmik.vesilasku.test.TestResource
import org.scalatest.{FlatSpec, Matchers}

class BillingServiceSpec extends FlatSpec with Matchers with TestResource {
  val readingTolerance = 0.001

  behavior of "The BillingService object"

  it should "calculate monthly readings" in {
    val hotMeter = Meter(MeterType.Hot, "1001")
    val coldMeter = Meter(MeterType.Cold, "1002")
    val apartment = Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_2001_2017-07-01-581.rlv").getPath)

    val readings = BillingService.monthlyReadings(apartment, csv, YearMonth.of(2017, Month.JUNE), MonthCount(6))

    val expectedMeters = Map(
      hotMeter -> List(
        Reading(EndOfMonthReading(74.371), Consumption(0.639)),
        Reading(EndOfMonthReading(73.732), Consumption(0.703)),
        Reading(EndOfMonthReading(73.029), Consumption(0.754)),
        Reading(EndOfMonthReading(72.275), Consumption(0.596)),
        Reading(EndOfMonthReading(71.679), Consumption(0.610)),
        Reading(EndOfMonthReading(71.069), Consumption(0.962))),
      coldMeter -> List(
        Reading(EndOfMonthReading(83.564), Consumption(0.809)),
        Reading(EndOfMonthReading(82.755), Consumption(1.396)),
        Reading(EndOfMonthReading(81.359), Consumption(1.615)),
        Reading(EndOfMonthReading(79.744), Consumption(1.814)),
        Reading(EndOfMonthReading(77.93), Consumption(1.013)),
        Reading(EndOfMonthReading(76.917), Consumption(0.705)))
    )

    assertMeterReadingsMatch(readings.meters, expectedMeters)
  }

  it should "calculate monthly readings when not starting from latest reading" in {
    val coldMeter = Meter(MeterType.Cold, "1020")
    val hotMeter = Meter(MeterType.Hot, "1021")
    val apartment = Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_4001_2017-08-01-581.rlv").getPath)

    val readings = BillingService.monthlyReadings(apartment, csv, YearMonth.of(2017, Month.MAY), MonthCount(2))

    val expectedMeters = Map(
      coldMeter -> List(
        Reading(EndOfMonthReading(130.82), Consumption(2.416)),
        Reading(EndOfMonthReading(128.404), Consumption(2.340))),
      hotMeter -> List(
        Reading(EndOfMonthReading(55.123), Consumption(1.569)),
        Reading(EndOfMonthReading(53.554), Consumption(0.373)))
    )

    assertMeterReadingsMatch(readings.meters, expectedMeters)
  }

  it should "refuse to calculate readings if month is in future" in {
    val coldMeter = Meter(MeterType.Cold, "1020")
    val hotMeter = Meter(MeterType.Hot, "1021")
    val apartment = Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_4001_2017-08-01-581.rlv").getPath)

    assertThrows[IllegalArgumentException] {
      BillingService.monthlyReadings(apartment, csv, YearMonth.of(2017, Month.DECEMBER), MonthCount(2))
    }
  }

  it should "refuse to calculate readings if month is too far in the past" in {
    val coldMeter = Meter(MeterType.Cold, "1020")
    val hotMeter = Meter(MeterType.Hot, "1021")
    val apartment = Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_4001_2017-08-01-581.rlv").getPath)

    assertThrows[IllegalArgumentException] {
      BillingService.monthlyReadings(apartment, csv, YearMonth.of(2015, Month.DECEMBER), MonthCount(2))
    }
  }

  it should "produce report" in {
    val apartments = List[Apartment](
      Apartment("31", List(
        Meter("As 31 KV", MeterType.Cold, "1010"),
        Meter("As 31 LV", MeterType.Hot, "1011"))),
      Apartment("32", List(
        Meter("As 32.1 KV", MeterType.Cold, "1020"),
        Meter("As 32.1 LV", MeterType.Hot, "1021"),
        Meter("As 32.2 KV", MeterType.Cold, "1022"),
        Meter("As 32.2 LV", MeterType.Hot, "1023"))),
      Apartment("33", List(
        Meter("As 33 KV", MeterType.Cold, "1030"),
        Meter("As 33 LV", MeterType.Hot, "1031")))
    )
    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_3001_2017-07-03-581.rlv").getPath)
    val report = BillingService.report(apartments, csv, YearMonth.of(2017, Month.JUNE), MonthCount(6),
      WaterHeatingEnergy(58))

    toCsv(report.producer()) shouldEqual testFile("expected-report-01.csv")
  }

  def assertMeterReadingsMatch(actual: Map[Meter, List[Reading]], expected: Map[Meter, List[Reading]]): Unit = {
    def assertReadingsMatch(actual: List[Reading], expected: List[Reading]): Unit = {
      actual.length shouldEqual expected.length
      actual.zip(expected).foreach({ case (actualReading, expectedReading) =>
        actualReading.endOfMonth.reading shouldEqual expectedReading.endOfMonth.reading +- readingTolerance
        actualReading.consumption.consumption shouldEqual expectedReading.consumption.consumption +- readingTolerance
      })
    }

    actual.keySet shouldEqual expected.keySet
    expected.keySet.foreach(meter => {
      assertReadingsMatch(actual(meter), expected(meter))
    })
  }

}
