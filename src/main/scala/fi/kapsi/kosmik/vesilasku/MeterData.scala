package fi.kapsi.kosmik.vesilasku

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fi.kapsi.kosmik.vesilasku.csv.{Csv, Row => CsvRow, fromFile => fromCsvFile}

object MeterData {
  val colNames: List[String] = List(Columns.identificationNumber, Columns.readoutDate) ++
    (1 to 15).map(i => Columns.monthlyVolume(i)).toList

  def fromFile(path: String): MeterData = new MeterData(fromCsvFile(path, colNames, '\t'))
}

class MeterData(private val csv: Csv) {

  class Row(private val csvRow: CsvRow) {
    def identificationNumber(): String = csvRow.col(Columns.identificationNumber)

    def monthlyVolume(monthsFromNow: Int): Double =
      Parser.parseDouble(csvRow.col(Columns.monthlyVolume(monthsFromNow)))

    def readoutDate(): LocalDate = Parser.parseDate(csvRow.col(Columns.readoutDate))
  }

  def rows(): Stream[Row] = csv.rows().map(csvRow => new Row(csvRow))
}

/**
  * Functions for parsing values using meter data specific formatting.
  */
private object Parser {
  /** DateTimeFormatter is thread safe. */
  private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  def parseDate(formatted: String): _root_.java.time.LocalDate = LocalDate.parse(formatted, dateFormatter)

  def parseDouble(formatted: String): Double = formatted.replace(",", ".").toDouble
}

private object Columns {
  private val monthRange: Int = 15

  def identificationNumber = "Identification number"

  def monthlyVolume(monthsFromNow: Int): String = {
    if (monthsFromNow > monthRange) {
      throw new IllegalArgumentException(s"Month $monthsFromNow is out of range")
    }
    s"Monthly volume ${-monthsFromNow}"
  }

  def readoutDate = "Readout date"
}
