package fi.kapsi.kosmik.vesilasku

import org.scalatest.{FlatSpec, Matchers}

class VesilaskuSpec extends FlatSpec with Matchers {
  "The Csv object" should "read csv from file" in {
    val csv = Csv.fromFile(getClass.getResource("data-sample.csv").getPath, '\t')
    csv.header shouldEqual List("Col1", "Col2", "Col3")
    csv.rows shouldEqual List(
      List("v11", "v12", "v13"),
      List("v21", "v22", "v23")
    )
  }
}
