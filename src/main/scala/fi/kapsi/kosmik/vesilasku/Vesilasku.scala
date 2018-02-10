package fi.kapsi.kosmik.vesilasku

import java.time.{Month, YearMonth}

import fi.kapsi.kosmik.vesilasku.ConfigParsing.Config
import fi.kapsi.kosmik.vesilasku.csv.toCsv
import fi.kapsi.kosmik.vesilasku.model.{MonthCount, WaterHeatingEnergy}
import fi.kapsi.kosmik.vesilasku.report.Report

object Vesilasku extends App {
  ConfigParsing.parser.parse(args, Config()) match {
    case Some(config) =>
      val apartments = Apartments.fromFile(config.apartmentCsvPath)
      val csv = MeterData.fromFile(config.deviceValuesCsv)
      val report = Report.forMonths(apartments, csv, YearMonth.of(config.year, Month.JUNE),
        config.months, config.waterHeatingEnergy)
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
                    month: Int = -1)

  val parser: scopt.OptionParser[Config] = new scopt.OptionParser[Config]("vesilasku") {
    head("vesilasku", "0.1")

    opt[Int]('c', "months")
      .action((x, c) => c.copy(months = MonthCount(x)))
      .text(s"number of months to include (default ${Defaults.months})")

    opt[Int]('w', "water-heating-energy")
      .action((x, c) => c.copy(waterHeatingEnergy = WaterHeatingEnergy(x)))
      .text(s"heating energy (kWh) per m^3 of hot water (default ${Defaults.waterHeatingEnergy} kWh/m^3)")

    opt[Int]('y', "year")
      .required()
      .action((x, c) => c.copy(year = x))
      .text(s"year of the last month in the report")

    opt[Int]('m', "month")
      .required()
      .action((x, c) => c.copy(month = x))
      .validate(x => {
        if (x >= 1 && x <= 12) success
        else failure("month must be between 1 and 12")
      })
      .text(s"last month in the report (between 1 and 12")

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
