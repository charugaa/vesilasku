package fi.kapsi.kosmik.vesilasku.report

import java.time.YearMonth

import fi.kapsi.kosmik.vesilasku.MeterData
import fi.kapsi.kosmik.vesilasku.csv.CsvProducer
import fi.kapsi.kosmik.vesilasku.model.MeterType.MeterType
import fi.kapsi.kosmik.vesilasku.model._
import fi.kapsi.kosmik.vesilasku.report.Readings._

import scala.annotation.tailrec

object Report {
  def forMonths(apartments: List[Apartment], csv: MeterData, fromMonth: YearMonth,
                months: MonthCount, heatingEnergy: WaterHeatingEnergy): Report = {
    val apartmentsMonthlyReadings = apartments
      .map(ap => (ap, monthlyReadings(ap, csv, fromMonth, months).meters))

    val allMeterReadings = apartmentsMonthlyReadings
      .flatMap({ case (_, meterReadings) => meterReadings })

    val totalConsumptions = allMeterReadings
      .map(meterReadings => meterReadings._2)
      .map(readings => readings.map(reading => reading.consumption))
      .reduce((totals, consumptions) => {
        totals.zip(consumptions).map({ case (t, c) => t + c })
      })

    val energyConsumptions = allMeterReadings
      .filter({ case (meter, _) => meter.meterType == MeterType.Hot })
      .map(meterReadings => meterReadings._2)
      .map(readings => readings.map(reading => reading.consumption))
      .reduce((totals, consumptions) => {
        totals.zip(consumptions).map({ case (t, c) => t + c })
      })
      .map(t => heatingEnergy.energyConsumption(t))

    new Report(fromMonth, months, apartmentsMonthlyReadings.toMap, totalConsumptions, energyConsumptions)
  }
}

class Report(private val fromMonth: YearMonth,
             private val months: MonthCount,
             private val apartmentReports: Map[Apartment, Map[Meter, List[Reading]]],
             private val totalConsumptions: List[Consumption],
             private val energyConsumptions: List[KiloWh]) {
  def producer(): CsvProducer = new CsvProducer {

    override def rows(): List[List[String]] = {
      val allMeterReadings = collectMeterReadings()
      val allMeters = collectMeters(allMeterReadings)
      val summaryRows = formatSummaryRows()
      prependMeterRows(summaryRows, allMeterReadings, allMeters)
    }

    override def header(): List[String] = {
      @tailrec
      def monthHeaders(acc: List[String], curr: YearMonth, rem: Int): List[String] = {
        if (rem == 0) acc
        else {
          monthHeaders(s"${curr.getMonthValue}-${curr.getYear}" :: acc, curr.minusMonths(1), rem - 1)
        }
      }

      "Meter label" :: "Meter id" :: "Meter type" :: monthHeaders(Nil, fromMonth, months.count)
    }

    private def collectMeterReadings() = {
      apartmentReports
        .values
        .foldLeft(Map[Meter, List[Reading]]())((allMeters, apMeters) => allMeters ++ apMeters)
    }

    private def collectMeters(meterReadings: Map[Meter, scala.List[Reading]]) = {
      meterReadings.keys.toList.sortWith((a, b) => a.label.compareTo(b.label) < 0)
    }

    private def formatSummaryRows(): List[List[String]] = {
      val totalM3 = List[String]("total m^3", "", "") ++ totalConsumptions.reverse.map(tc => formatConsumption(tc))
      val totalKWh = List[String]("total kWh", "", "") ++ energyConsumptions.reverse.map(ec => formatEnergy(ec))
      totalM3 :: totalKWh :: Nil
    }

    private def prependMeterRows(summaryRows: List[List[String]],
                                 allMeterReadings: Map[Meter, List[Reading]],
                                 allMeters: List[Meter]) = {
      @tailrec
      def rec(acc: List[List[String]], rem: List[Meter]): List[List[String]] = {
        if (rem.isEmpty) acc
        else {
          val meter = rem.head
          val formattedRow = List(meter.label, meter.radioId, format(meter.meterType)) ++
            allMeterReadings(meter)
              .reverse
              .map(r => formatConsumption(r.consumption))
          rec(formattedRow :: acc, rem.tail)
        }
      }

      rec(summaryRows, allMeters.reverse)
    }
  }

  private def formatConsumption(value: Consumption): String = "%.3f".format(value.consumption).replaceAll("\\.", ",")

  private def formatEnergy(value: KiloWh): String = "%.3f".format(value.value).replaceAll("\\.", ",")

  private def format(meterType: MeterType): String = meterType match {
    case MeterType.Cold => "cold"
    case MeterType.Hot => "hot"
  }
}