package fi.kapsi.kosmik.vesilasku

import org.scalatest.{FlatSpec, Matchers}

class BillingServiceSpec extends FlatSpec with Matchers {
  val readingTolerance = 0.001

  behavior of "The BillingService object"

  it should "calculate monthly readings" in {
    val hotMeter = Meter(MeterType.Hot, "1001")
    val coldMeter = Meter(MeterType.Cold, "1002")
    val apartment = new Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterCsv.fromFile(getClass.getResource("DevicesValues646_2001_2017-07-01-581.rlv").getPath)

    val readings = BillingService.monthlyReadings(apartment, csv, 6)

    val expectedMeters = Map(
      hotMeter -> List(
        Reading(74.371, 0.639),
        Reading(73.732, 0.703),
        Reading(73.029, 0.754),
        Reading(72.275, 0.596),
        Reading(71.679, 0.610),
        Reading(71.069, 0.962)),
      coldMeter -> List(
        Reading(83.564, 0.809),
        Reading(82.755, 1.396),
        Reading(81.359, 1.615),
        Reading(79.744, 1.814),
        Reading(77.93, 1.013),
        Reading(76.917, 0.705))
    )

    assertMeterReadingsMatch(readings.meters, expectedMeters)
  }

  def assertMeterReadingsMatch(actual: Map[Meter, List[Reading]], expected: Map[Meter, List[Reading]]): Unit = {
    def assertReadingsMatch(actual: List[Reading], expected: List[Reading]): Unit = {
      actual.length shouldEqual expected.length
      actual.zip(expected).foreach({ case (actualReading, expectedReading) =>
        actualReading.endOfMonth shouldEqual expectedReading.endOfMonth +- readingTolerance
        actualReading.consumption shouldEqual expectedReading.consumption +- readingTolerance
      })
    }

    actual.keySet shouldEqual expected.keySet
    expected.keySet.foreach(meter => {
      assertReadingsMatch(actual(meter), expected(meter))
    })
  }

}
