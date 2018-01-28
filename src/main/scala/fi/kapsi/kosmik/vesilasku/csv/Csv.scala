package fi.kapsi.kosmik.vesilasku.csv

class Csv(val header: List[String], val rawRows: List[List[String]]) {
  def rows(): Stream[Row] = rowsStream(header, rawRows)

  private def rowsStream(header: List[String], rows: List[List[String]]): scala.Stream[Row] =
    if (rows.isEmpty) Stream.empty
    else new Row(header, rows.head) #:: rowsStream(header, rows.tail)
}
