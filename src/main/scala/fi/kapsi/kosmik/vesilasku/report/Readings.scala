package fi.kapsi.kosmik.vesilasku.report

import java.time.temporal.ChronoUnit
import java.time.{LocalDate, YearMonth}

import fi.kapsi.kosmik.vesilasku.MeterData
import fi.kapsi.kosmik.vesilasku.MeterData.Row
import fi.kapsi.kosmik.vesilasku.model._

object Readings {
  def monthlyReadings(apartment: Apartment, csv: MeterData, fromMonth: YearMonth, months: MonthCount):
  MonthlyReadings = {
    new MonthlyReadings(
      endOfMonthReadings(apartment, csv.rows(), fromMonth, months)
        .map(calculateConsumptions)
    )
  }

  private def calculateConsumptions(me: (Meter, List[EndOfMonthReading])) = me match {
    case (meter, endOfMonthValues) =>
      meter -> endOfMonthValues.zip(endOfMonthValues.tail).map({
        case (month, lastMonth) => Reading(month, Consumption(month.reading - lastMonth.reading))
      })
  }

  private def endOfMonthReadings(apartment: Apartment, rows: Stream[Row], fromMonth: YearMonth, months: MonthCount) = {
    rows
      .map(row => meterRows(row, apartment))
      .filter(mr => mr.isDefined)
      .map(mr => mr.get)
      .map(mr => meterEndOfMonthValues(mr, fromMonth, months))
      .toMap
  }

  private def meterEndOfMonthValues(meterRow: (Meter, Row), fromMonth: YearMonth, months: MonthCount) = meterRow match {
    case (meter, row) =>
      val monthOffset = readoutMonthOffset(fromMonth, meter, row.readoutDate())
      val values = (monthOffset to months.count + monthOffset)
        .map(monthsFromNow => row.endOfMonthReading(monthsFromNow))
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
  def apply(endOfMonth: EndOfMonthReading, consumption: Consumption): Reading = {
    new Reading(endOfMonth, consumption)
  }
}

class Reading(val endOfMonth: EndOfMonthReading, val consumption: Consumption) {
  override def toString = s"Reading($endOfMonth, $consumption)"
}

/**
  * @param meters a list of monthly Readings, newest first
  */
class MonthlyReadings(val meters: Map[Meter, List[Reading]])