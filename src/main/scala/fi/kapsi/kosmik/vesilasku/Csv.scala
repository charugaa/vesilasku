package fi.kapsi.kosmik.vesilasku

import scala.io.Source

object Csv {
  def fromFile(path: String, requiredColNames: List[String], separator: Char): Csv = {
    val rawRows = Source.fromFile(path).getLines().toList

    val header = splitRow(rawRows.head, separator)
    assertColNames(requiredColNames, header)

    val rows = rawRows.tail.map(row => splitRow(row, separator))

    rows.foreach(row => {
      if (row.length != header.length) {
        throw new IllegalArgumentException("not all rows have an equal number of columns")
      }
    })

    new Csv(header, rows)
  }

  private def assertColNames(requiredColNames: List[String], header: List[String]): Unit = {
    requiredColNames.foreach(colName => {
      if (!header.contains(colName)) {
        throw new MissingColumnException(colName)
      }
    })
  }

  private def splitRow(row: String, separator: Char): List[String] = row.split(separator).toList
}

class MissingColumnException(val colName: String) extends Exception

class Csv(val header: List[String], val rows: List[List[String]]) {
}