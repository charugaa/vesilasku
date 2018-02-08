package fi.kapsi.kosmik.vesilasku.report

import java.time.{Month, YearMonth}

import fi.kapsi.kosmik.vesilasku.csv.toCsv
import fi.kapsi.kosmik.vesilasku.model._
import fi.kapsi.kosmik.vesilasku.test.TestResource
import fi.kapsi.kosmik.vesilasku.{Apartments, MeterData}
import org.scalatest.{FlatSpec, Matchers}

class ReportSpec extends FlatSpec with Matchers with TestResource {
  behavior of "The Report object"

  it should "produce report" in {
    val apartments = Apartments.fromFile(getClass.getResource("apartments-01.csv").getPath)
    val csv = MeterData.fromFile(getClass.getResource("DevicesValues646_3001_2017-07-03-581.rlv").getPath)
    val report = Report.forMonths(apartments, csv, YearMonth.of(2017, Month.JUNE), MonthCount(6),
      WaterHeatingEnergy(58))

    toCsv(report.producer()) shouldEqual testFile("expected-report-01.csv")
  }
}
