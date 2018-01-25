package fi.kapsi.kosmik.vesilasku

object BillingService {
  def monthlyReadings(apartment: Apartment, csv: Csv, months: Int): MonthlyReadings = {
    val endOfMonthReadings = csv.rows()
      .map(row => {
        val rowMeter = row.col("Identification number")
        apartment.meters.find((meter) => meter.radioId == rowMeter)
          .map(meter => (meter, row))
      })
      .filter(mr => mr.isDefined)
      .map(mr => mr.get)
      .map({
        case (meter, row) => {
          val values = (-1 to -(months + 1) by -1)
            .map(monthIndex => s"Monthly volume $monthIndex")
            .map(monthCol => row.col(monthCol.toString))
            .map(doubleWithComma => doubleWithComma.replaceAll(",", ".")) // TODO: less hackish number format handling
            .map(_.toDouble)
            .toList
          (meter, values)
        }
      })
      .toMap

    val values = endOfMonthReadings
      .map({
        case (meter, endOfMonthValues) => {
          meter -> endOfMonthValues.zip(endOfMonthValues.tail).map({
            case (month, lastMonth) => new Reading(month, month - lastMonth)
          })
        }
      })

    new MonthlyReadings(values)
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