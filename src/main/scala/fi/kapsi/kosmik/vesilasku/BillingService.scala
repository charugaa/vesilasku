package fi.kapsi.kosmik.vesilasku

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, YearMonth}

import fi.kapsi.kosmik.vesilasku.MeterData.Row
import fi.kapsi.kosmik.vesilasku.csv.CsvProducer
import fi.kapsi.kosmik.vesilasku.model.MeterType.MeterType
import fi.kapsi.kosmik.vesilasku.model.{Apartment, Meter, MeterType}

import scala.annotation.tailrec

object BillingService {
  def monthlyReadings(apartment: Apartment, csv: MeterData, fromMonth: YearMonth, months: Int): MonthlyReadings = {
    new MonthlyReadings(
      endOfMonthReadings(apartment, csv.rows(), fromMonth, months)
        .map(calculateReadings)
    )
  }

  def report(apartments: List[Apartment], csv: MeterData, fromMonth: YearMonth,
             months: Int, kWhPerM3: Double): Report = {
    val apartmentsMonthlyReadings = apartments
      .map(ap => (ap, monthlyReadings(ap, csv, fromMonth, months).meters))

    val allMeterReadings = apartmentsMonthlyReadings
      .flatMap({ case (_, meterReadings) => meterReadings })

    val totalConsumptions = allMeterReadings
      .map(meterReadings => meterReadings._2)
      .map(readings => readings.map(reading => reading.consumption))
      .reduce((totals, consumptions) => {
        totals.zip(consumptions).map({ case (t, c) => t + c })
      })

    val powerConsumptions = allMeterReadings
      .filter({ case (meter, _) => meter.meterType == MeterType.Hot })
      .map(meterReadings => meterReadings._2)
      .map(readings => readings.map(reading => reading.consumption))
      .reduce((totals, consumptions) => {
        totals.zip(consumptions).map({ case (t, c) => t + c })
      })
      .map(t => t * kWhPerM3)

    new Report(fromMonth, months, apartmentsMonthlyReadings.toMap, totalConsumptions, powerConsumptions)
  }

  private def calculateReadings(me: (Meter, List[Double])) = me match {
    case (meter, endOfMonthValues) =>
      meter -> endOfMonthValues.zip(endOfMonthValues.tail).map({
        case (month, lastMonth) => new Reading(month, month - lastMonth)
      })
  }

  private def endOfMonthReadings(apartment: Apartment, rows: Stream[Row], fromMonth: YearMonth, months: Int) = {
    rows
      .map(row => meterRows(row, apartment))
      .filter(mr => mr.isDefined)
      .map(mr => mr.get)
      .map(mr => meterEndOfMonthValues(mr, fromMonth, months))
      .toMap
  }

  private def meterEndOfMonthValues(meterRow: (Meter, Row), fromMonth: YearMonth, months: Int) = meterRow match {
    case (meter, row) =>
      val monthOffset = readoutMonthOffset(fromMonth, meter, row.readoutDate())
      val values = (monthOffset to months + monthOffset)
        .map(monthsFromNow => row.monthlyVolume(monthsFromNow))
        .toList
      (meter, values)
  }

  private def readoutMonthOffset(fromMonth: YearMonth, meter: Meter, readOutDate: LocalDate): Int = {
    val readoutMonth = YearMonth.of(readOutDate.getYear, readOutDate.getMonth)

    if (!fromMonth.isBefore(readoutMonth)) {
      throw new IllegalArgumentException(s"can not get values for meter $meter from $fromMonth as readout date " +
        s"is $readOutDate")
    }

    fromMonth.until(readoutMonth, ChronoUnit.MONTHS).intValue()
  }

  private def meterRows(row: Row, apartment: Apartment): Option[(Meter, Row)] = {
    val rowMeter = row.identificationNumber()
    apartment.meters.find((meter) => meter.radioId == rowMeter)
      .map(meter => (meter, row))
  }

}

object Reading {
  def apply(endOfMonth: Double, consumption: Double): Reading = {
    new Reading(endOfMonth, consumption)
  }
}

class Reading(val endOfMonth: Double, val consumption: Double) {
  override def toString = s"Reading($endOfMonth, $consumption)"
}

/**
  * @param meters a list of monthly Readings, newest first
  */
class MonthlyReadings(val meters: Map[Meter, List[Reading]])

class Report(private val fromMonth: YearMonth,
             private val months: Int,
             private val apartmentReports: Map[Apartment, Map[Meter, List[Reading]]],
             private val totalConsumptions: List[Double],
             private val powerConsumptions: List[Double]) {
  def producer(): CsvProducer = new CsvProducer {

    override def rows(): List[List[String]] = {
      val allMeterReadings = collectMeterReadings()
      val allMeters = collectMeters(allMeterReadings)
      val summaryRows = formatSummaryRows()
      prependMeterRows(summaryRows, allMeterReadings, allMeters)
    }

    override def header(): List[String] = {
      @tailrec
      def monthHeaders(acc: List[String], curr: YearMonth, rem: Int): List[String] = {
        if (rem == 0) acc
        else {
          monthHeaders(s"${curr.getMonthValue}-${curr.getYear}" :: acc, curr.minusMonths(1), rem - 1)
        }
      }

      "Meter label" :: "Meter id" :: "Meter type" :: monthHeaders(Nil, fromMonth, months)
    }

    private def collectMeterReadings() = {
      apartmentReports
        .values
        .foldLeft(Map[Meter, List[Reading]]())((allMeters, apMeters) => allMeters ++ apMeters)
    }

    private def collectMeters(meterReadings: Map[Meter, scala.List[Reading]]) = {
      meterReadings.keys.toList.sortWith((a, b) => a.label.compareTo(b.label) < 0)
    }

    private def formatSummaryRows(): List[List[String]] = {
      val totalM3 = List[String]("total m^3", "", "") ++ totalConsumptions.reverse.map(tc => formatReading(tc))
      val totalKWh = List[String]("total kWh", "", "") ++ powerConsumptions.reverse.map(tc => formatReading(tc))
      totalM3 :: totalKWh :: Nil
    }

    private def prependMeterRows(summaryRows: List[List[String]],
                                 allMeterReadings: Map[Meter, List[Reading]],
                                 allMeters: List[Meter]) = {
      @tailrec
      def rec(acc: List[List[String]], rem: List[Meter]): List[List[String]] = {
        if (rem.isEmpty) acc
        else {
          val meter = rem.head
          val formattedRow = List(meter.label, meter.radioId, format(meter.meterType)) ++
            allMeterReadings(meter)
              .reverse
              .map(r => formatReading(r.consumption))
          rec(formattedRow :: acc, rem.tail)
        }
      }

      rec(summaryRows, allMeters.reverse)
    }
  }

  private def formatReading(consumption: Double): String = "%.3f".format(consumption).replaceAll("\\.", ",")

  private def format(meterType: MeterType): String = meterType match {
    case MeterType.Cold => "cold"
    case MeterType.Hot => "hot"
  }
}