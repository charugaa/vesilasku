package fi.kapsi.kosmik.vesilasku

object MeterCsv {
  val colNames: List[String] = List("Identification number") ++ (-1 to -15 by -1).map(i => s"Monthly volume $i").toList

  def fromFile(path: String): Csv = Csv.fromFile(path, colNames, '\t')
}
