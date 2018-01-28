package fi.kapsi.kosmik.vesilasku

package object model {

  class EndOfMonthReading(val reading: Double) extends AnyVal

  object EndOfMonthReading {
    def apply(reading: Double) = new EndOfMonthReading(reading)
  }

  class Consumption(val consumption: Double) extends AnyVal {
    def +(other: Consumption) = Consumption(consumption + other.consumption)

    def *(mul: Double) = Consumption(consumption * mul)
  }

  object Consumption {
    def apply(consumption: Double) = new Consumption(consumption)
  }

  class KiloWh(val value: Double) extends AnyVal

  object KiloWh {
    def apply(value: Double) = new KiloWh(value)
  }

  class WaterHeatingEnergy(val kiloWhPerCubicMeter: Int) extends AnyVal {
    def energyConsumption(consumption: Consumption): KiloWh = KiloWh(kiloWhPerCubicMeter * consumption.consumption)
  }

  object WaterHeatingEnergy {
    def apply(kiloWhPerCubicMeter: Int) = new WaterHeatingEnergy(kiloWhPerCubicMeter)
  }

  class MonthCount(val count: Int) extends AnyVal

  object MonthCount {
    def apply(count: Int) = new MonthCount(count)
  }

}
