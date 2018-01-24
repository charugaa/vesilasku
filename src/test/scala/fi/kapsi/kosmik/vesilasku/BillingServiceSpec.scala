package fi.kapsi.kosmik.vesilasku

import org.scalatest.{FlatSpec, Matchers}

class BillingServiceSpec extends FlatSpec with Matchers {
  behavior of "The BillingService object"

  it should "calculate monthly readings" in {
    val hotMeter = Meter(MeterType.Hot, "1001")
    val coldMeter = Meter(MeterType.Cold, "1002")
    val apartment = new Apartment("42", List(hotMeter, coldMeter))

    val csv = MeterCsv.fromFile(getClass.getResource("DevicesValues646_2001_2017-07-01-581.rlv").getPath)

    val readings = BillingService.monthlyReadings(apartment, csv, 6)

    // TODO: make this assertion nicer to read
    // TODO: probably replace Reading.equals with a custom assertion
    readings.values shouldEqual Map(
      hotMeter -> List(
        Reading(74.371, 0.6389999999999958),
        Reading(73.732, 0.703000000000003),
        Reading(73.029, 0.7539999999999907),
        Reading(72.275, 0.5960000000000036),
        Reading(71.679, 0.6099999999999994),
        Reading(71.069, 0.9620000000000033)),
      coldMeter -> List(
        Reading(83.564, 0.8089999999999975),
        Reading(82.755, 1.3960000000000008),
        Reading(81.359, 1.6149999999999949),
        Reading(79.744, 1.813999999999993),
        Reading(77.93, 1.0130000000000052),
        Reading(76.917, 0.7049999999999983))
    )
  }
}
