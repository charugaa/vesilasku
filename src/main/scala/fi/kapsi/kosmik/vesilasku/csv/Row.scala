package fi.kapsi.kosmik.vesilasku.csv

class Row(private val header: List[String], private val rawRow: List[String]) {
  def col(name: String): String = {
    val index = header.indexOf(name)
    if (index < 0) {
      throw new IllegalArgumentException(f"no such column exists: $name")
    }

    rawRow(index)
  }
}
