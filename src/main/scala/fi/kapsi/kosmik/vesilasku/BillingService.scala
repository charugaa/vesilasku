package fi.kapsi.kosmik.vesilasku

import fi.kapsi.kosmik.vesilasku.MeterData.Row

object BillingService {
  def monthlyReadings(apartment: Apartment, csv: MeterData, months: Int): MonthlyReadings = {
    new MonthlyReadings(
      endOfMonthReadings(apartment, csv.rows(), months)
        .map(calculateReadings)
    )
  }

  private def calculateReadings(me: (Meter, List[Double])) = me match {
    case (meter, endOfMonthValues) =>
      meter -> endOfMonthValues.zip(endOfMonthValues.tail).map({
        case (month, lastMonth) => new Reading(month, month - lastMonth)
      })
  }

  private def endOfMonthReadings(apartment: Apartment, rows: Stream[Row], months: Int): Map[Meter, List[Double]] = {
    rows
      .map(row => meterRows(row, apartment))
      .filter(mr => mr.isDefined)
      .map(mr => mr.get)
      .map(mr => meterEndOfMonthValues(mr, months))
      .toMap
  }

  private def meterEndOfMonthValues(meterRow: (Meter, Row), months: Int): (Meter, List[Double]) = meterRow match {
    case (meter, row) =>
      val values = (1 to months + 1)
        .map(monthsFromNow => row.monthlyVolume(monthsFromNow))
        .toList
      (meter, values)
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