package bean.entity

case class Amount(n: BigDecimal, ccy: String) {
	override def toString: String = s"${n.bigDecimal.stripTrailingZeros().toPlainString} $ccy"

	def unary_- : Amount = Amount(-n, ccy)
	def *(m: BigDecimal): Amount = Amount(n * m, ccy)
}

object Amount {
	def sum(as: Seq[Amount]): Amount = {
		assert(as.nonEmpty)
		val ccy = as.head.ccy
		assert(as.forall(_.ccy == ccy))

		val n = as.map(_.n).sum
		Amount(n, ccy)
	}
}
