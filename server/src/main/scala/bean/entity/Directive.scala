package bean.entity

import java.time.LocalDate

// n: 从1开始
case class Src(file: String, n: Int) {
	override def toString: String = s"$file:$n"
}

trait Directive {
	def date: LocalDate
	def src: Src
}

case class Balance(date: LocalDate, account: String, amount: Amount, src: Src) extends Directive
case class AccountClose(date: LocalDate, account: String, src: Src) extends Directive
case class Trx(date: LocalDate, postings: Seq[Posting], narration: String, src: Src) extends Directive
case class Posting(account: String, delta: Amount, price: Option[Amount])
