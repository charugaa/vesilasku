package fi.kapsi.kosmik.vesilasku

import fi.kapsi.kosmik.vesilasku.csv.{Csv, Row => CsvRow, fromFile => fromCsvFile}

object MeterData {
  val colNames: List[String] = List(Colums.identificationNumber) ++ (1 to 15).map(i => Colums.monthlyVolume(i)).toList

  def fromFile(path: String): MeterData = new MeterData(fromCsvFile(path, colNames, '\t'))
}

class MeterData(private val csv: Csv) {

  class Row(private val csvRow: CsvRow) {
    def identificationNumber(): String = csvRow.col(Colums.identificationNumber)

    def monthlyVolume(monthsFromNow: Int): Double =
      csvRow.col(Colums.monthlyVolume(monthsFromNow)).replace(",", ".").toDouble
  }

  def rows(): Stream[Row] = csv.rows().map(csvRow => new Row(csvRow))
}

private object Colums {
  val monthRange: Int = 15

  val identificationNumber = "Identification number"

  def monthlyVolume(monthsFromNow: Int): String = {
    if (monthsFromNow > monthRange) {
      throw new IllegalArgumentException(s"Month $monthsFromNow is out of range")
    }
    s"Monthly volume ${-monthsFromNow}"
  }
}
