package fi.kapsi.kosmik.vesilasku.model

import fi.kapsi.kosmik.vesilasku.model.MeterType.MeterType

class Meter(val label: String, val meterType: MeterType, val radioId: String) {
  override def toString = s"Meter($meterType, $radioId)"

  def canEqual(other: Any): Boolean = other.isInstanceOf[Meter]

  override def equals(other: Any): Boolean = other match {
    case that: Meter =>
      (that canEqual this) &&
        meterType == that.meterType &&
        radioId == that.radioId
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(meterType, radioId)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

object Meter {
  def apply(meterType: MeterType, radioId: String): Meter = {
    new Meter(s"$radioId-$meterType", meterType, radioId)
  }

  def apply(label: String, meterType: MeterType, radioId: String): Meter = {
    new Meter(label, meterType, radioId)
  }
}

object MeterType extends Enumeration {
  type MeterType = Value
  val Cold: Value = Value("KV")
  val Hot: Value = Value("LV")
}
