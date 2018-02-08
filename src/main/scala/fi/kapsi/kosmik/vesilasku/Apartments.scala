package fi.kapsi.kosmik.vesilasku

import fi.kapsi.kosmik.vesilasku.csv.{Csv, Row => CsvRow, fromFile => fromCsvFile}
import fi.kapsi.kosmik.vesilasku.model.MeterType.MeterType
import fi.kapsi.kosmik.vesilasku.model.{Apartment, Meter, MeterType}

import scala.annotation.tailrec

object Apartments {
  def fromFile(path: String): List[Apartment] = {
    val csv = new ApartmentCsv(fromCsvFile(path, ApartmentCsv.colNames, ';'))
    val apartmentRowsByApartment = csv.rows()
      .map(row => (row.apartment(), Meter(row.meterLabel(), row.meterType(), row.meterId())))
      .groupBy(ar => ar._1)
      .toList
    buildApartments(List(), apartmentRowsByApartment)
  }

  @tailrec
  def buildApartments(acc: List[Apartment],
                      rem: List[(String, Stream[(String, Meter)])]): List[Apartment] = {
    if (rem.isEmpty) acc
    else {
      val meters = rem.head._2.toList
        .map(ar => ar._2)
      val apartment = Apartment(rem.head._1, meters)
      buildApartments(apartment :: acc, rem.tail)
    }

  }
}

private object ApartmentCsv {

  object Columns {
    def apartment = "Apartment"

    def meterId = "Meter id"

    def meterLabel = "Meter label"

    def meterType = "Meter type"
  }

  val colNames: List[String] = Columns.apartment :: Columns.meterId :: Columns.meterId :: Nil

  class Row(private val csvRow: CsvRow) {
    def apartment(): String = csvRow.col(Columns.apartment)

    def meterId(): String = csvRow.col(Columns.meterId)

    def meterLabel(): String = csvRow.col(Columns.meterLabel)

    def meterType(): MeterType = MeterType.withName(csvRow.col(Columns.meterType))
  }

}

private class ApartmentCsv(private val csv: Csv) {
  def rows(): Stream[ApartmentCsv.Row] = csv.rows().map(csvRow => new ApartmentCsv.Row(csvRow))
}

