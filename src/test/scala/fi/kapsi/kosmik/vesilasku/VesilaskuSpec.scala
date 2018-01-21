package fi.kapsi.kosmik.vesilasku

import org.scalatest.{FlatSpec, Matchers}

class VesilaskuSpec extends FlatSpec with Matchers {
  behavior of "The Csv object"

  it should "fail if required col names are missing" in {
    val colNames = List("Col1", "Col4")
    assertThrows[MissingColumnException] {
      Csv.fromFile(getClass.getResource("data-sample.csv").getPath, colNames, '\t')
    }
  }

  it should "read csv from file" in {
    val colNames = List("Col1", "Col2", "Col3")

    val csv = Csv.fromFile(getClass.getResource("data-sample.csv").getPath, colNames, '\t')

    csv.header shouldEqual colNames
    csv.rows shouldEqual List(
      List("v11", "v12", "v13"),
      List("v21", "v22", "v23")
    )
  }
}
