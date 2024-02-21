package bean.entity

import java.time.LocalDate

trait Directive {
}

case class Balance(date: LocalDate, account: String, amount: Amount) extends Directive
case class AccountClose(date: LocalDate, account: String) extends Directive
case class Trx(date: LocalDate, postings: Seq[Posting], narration: String) extends Directive
case class Posting(account: String, delta: Amount, price: Option[Amount])
