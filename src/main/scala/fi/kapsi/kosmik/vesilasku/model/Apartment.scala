package fi.kapsi.kosmik.vesilasku.model

class Apartment(val label: String, val meters: List[Meter])

object Apartment {
  def apply(label: String, meters: List[Meter]): Apartment = new Apartment(label, meters)
}