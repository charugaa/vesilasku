package fi.kapsi.kosmik.vesilasku

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fi.kapsi.kosmik.vesilasku.MeterData.Row
import fi.kapsi.kosmik.vesilasku.csv.{Csv, Row => CsvRow, fromFile => fromCsvFile}
import fi.kapsi.kosmik.vesilasku.model.EndOfMonthReading

object MeterData {

  class Row(private val csvRow: CsvRow) {
    def identificationNumber(): String = csvRow.col(Columns.identificationNumber)

    def endOfMonthReading(monthsFromReadout: Int): EndOfMonthReading =
      EndOfMonthReading(Parser.parseDouble(csvRow.col(Columns.monthlyVolume(monthsFromReadout))))

    def readoutDate(): LocalDate = Parser.parseDate(csvRow.col(Columns.readoutDate))
  }

  def fromFile(path: String): MeterData = new MeterData(fromCsvFile(path, colNames, '\t'))

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

    def monthlyVolume(monthsFromReadout: Int): String = {
      if (monthsFromReadout > monthRange) {
        throw new IllegalArgumentException(s"Month $monthsFromReadout is out of range")
      }
      s"Monthly volume ${-monthsFromReadout}"
    }

    def readoutDate = "Readout date"
  }

  private val colNames: List[String] = List(Columns.identificationNumber, Columns.readoutDate) ++
    (1 to 15).map(i => Columns.monthlyVolume(i)).toList
}

class MeterData(private val csv: Csv) {
  def rows(): Stream[Row] = csv.rows().map(csvRow => new Row(csvRow))
}
