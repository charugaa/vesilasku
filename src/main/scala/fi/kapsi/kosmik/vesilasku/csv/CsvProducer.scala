package fi.kapsi.kosmik.vesilasku.csv

trait CsvProducer {
  def header(): List[String]

  def rows(): Stream[List[String]]
}
