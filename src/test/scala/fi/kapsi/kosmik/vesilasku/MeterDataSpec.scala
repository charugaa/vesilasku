package fi.kapsi.kosmik.vesilasku

import java.time.LocalDate

import org.scalatest.{FlatSpec, Matchers}

class MeterDataSpec extends FlatSpec with Matchers {
  val readingTolerance = 0.001

  behavior of "The MeterData object"

  it should "parse different meter data types" in {
    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_2001_2017-07-01-581.rlv").getPath)
    val firstRow = csv.rows().head

    firstRow.endOfMonthReading(1).reading shouldEqual 56.398 +- readingTolerance
    firstRow.readoutDate shouldEqual LocalDate.of(2017, 7, 2)
  }

}
