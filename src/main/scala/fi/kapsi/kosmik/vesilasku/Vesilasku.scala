package fi.kapsi.kosmik.vesilasku

import java.time.YearMonth

import fi.kapsi.kosmik.vesilasku.ConfigParsing.Config
import fi.kapsi.kosmik.vesilasku.csv.toCsv
import fi.kapsi.kosmik.vesilasku.model.{MonthCount, WaterHeatingEnergy}
import fi.kapsi.kosmik.vesilasku.report.Report

import scala.util.{Success, Try}

object Vesilasku extends App {
  ConfigParsing.parser.parse(args, Config()) match {
    case Some(config) =>
      val apartments = Apartments.fromFile(config.apartmentCsvPath)
      val csv = MeterData.fromFile(config.deviceValuesCsv)
      val report = Report.forMonths(apartments, csv, config.fromMonth, config.months, config.waterHeatingEnergy)
      println(toCsv(report.producer()))

    case None =>
    // arguments are bad, error message will have been displayed
  }
}

object ConfigParsing {

  object Defaults {
    val months = 6
    val waterHeatingEnergy = 58
  }

  case class Config(apartmentCsvPath: String = ".",
                    deviceValuesCsv: String = ".",
                    months: MonthCount = MonthCount(Defaults.months),
                    waterHeatingEnergy: WaterHeatingEnergy = WaterHeatingEnergy(Defaults.waterHeatingEnergy),
                    year: Int = -1,
                    fromMonth: YearMonth = YearMonth.now().minusMonths(1))

  val parser: scopt.OptionParser[Config] = new scopt.OptionParser[Config]("vesilasku") {
    head("vesilasku", "0.1")

    opt[Int]('c', "months")
      .action((x, c) => c.copy(months = MonthCount(x)))
      .text(s"number of months to include (default ${Defaults.months})")

    opt[Int]('w', "water-heating-energy")
      .action((x, c) => c.copy(waterHeatingEnergy = WaterHeatingEnergy(x)))
      .text(s"heating energy (kWh) per m^3 of hot water (default ${Defaults.waterHeatingEnergy} kWh/m^3)")

    opt[String]('m', "from-month")
      .action((x, c) => c.copy(fromMonth = YearMonth.parse(x)))
      .validate(x => {
        val m = Try(YearMonth.parse(x))
        m match {
          case Success(_) => success
          case _ => failure("year and month must be given in format yyyy-mm where month is between 1 and 12")
        }
      })
      .text("last month in the report in format yyyy-mm where month is between 1 and 12 (default is the month " +
        "before the current month)")

    opt[String]('a', "apartment-csv")
      .required()
      .valueName("<file>")
      .action((x, c) => c.copy(apartmentCsvPath = x))
      .text("csv file containing apartments")

    opt[String]('d', "device-values-csv")
      .required()
      .valueName("<file>")
      .action((x, c) => c.copy(deviceValuesCsv = x))
      .text("csv file containing device values")

    help("help").text("prints this usage text")
  }
}
