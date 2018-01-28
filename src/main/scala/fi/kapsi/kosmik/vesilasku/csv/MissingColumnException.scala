package fi.kapsi.kosmik.vesilasku.csv

class MissingColumnException(val colName: String) extends Exception(s"Missing column: $colName")

