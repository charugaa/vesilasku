package fi.kapsi.kosmik.vesilasku.csv

import org.scalatest.{FlatSpec, Matchers}

class CsvSpec extends FlatSpec with Matchers {
  behavior of "The Csv object"

  it should "fail if file does not exist" in {
    val colNames = List()
    assertThrows[IllegalArgumentException] {
      fromFile("/unknown/location/file.csv", colNames, '\t')
    }
  }

  it should "fail if required col names are missing" in {
    val colNames = List("Col1", "Col4")
    assertThrows[MissingColumnException] {
      fromFile(getClass.getResource("data-sample.csv").getPath, colNames, '\t')
    }
  }

  it should "read csv from file" in {
    val colNames = List("Col1", "Col2", "Col3")

    val csv = fromFile(getClass.getResource("data-sample.csv").getPath, colNames, '\t')

    csv.header shouldEqual colNames
    csv.rawRows shouldEqual List(
      List("v11", "v12", "v13"),
      List("v21", "v22", "v23")
    )
  }

  it should "parse csv into Rows with column access by name" in {
    val colNames = List("Col1", "Col3")

    val csv = fromFile(getClass.getResource("data-sample.csv").getPath, colNames, '\t')
    val rows = csv.rows()

    val row1 = rows.head
    row1.col("Col1") shouldEqual "v11"
    row1.col("Col2") shouldEqual "v12"

    val row2 = rows.tail.head
    row2.col("Col3") shouldEqual "v23"

    rows.tail.tail shouldEqual Nil
  }
}
