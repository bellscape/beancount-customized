package bean.entity

case class Amount(n: BigDecimal, ccy: String) {
	override def toString: String = s"$n $ccy"
}
