package bean.logic_b

import bean.entity._

import java.time.LocalDate
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object b3_validate_balance {

	// b 未必是已有 Balance 记录，可能来自 AccountClose 生成
	case class BalanceErr(date: LocalDate, account: String, expect_amount: Amount, missing: BigDecimal)

	def validate(input: Seq[Directive]): Seq[BalanceErr] = {
		val cache = new BalanceCache()
		val out = ArrayBuffer.empty[BalanceErr]
		input.foreach {
			case t: Trx => cache.add_trx(t)
			case b: Balance =>
				cache.get_opt(b.account, b.amount.ccy) match {
					case Some(current) =>
						if (b.amount.n != current) {
							out += BalanceErr(b.date, b.account, b.amount, b.amount.n - current)
							cache.set_balance(b.account, b.amount.ccy, b.amount.n)
						}
					case None =>
						cache.set_balance(b.account, b.amount.ccy, b.amount.n)
				}
			case c: AccountClose =>
				cache.list_all_ccy(c.account)
					.filter(_._2 != 0)
					.foreach { case (ccy, n) =>
						out += BalanceErr(c.date, c.account, Amount(zero, ccy), -n)
					}
		}
		out.toSeq
	}

	private val zero = BigDecimal(0)
	class BalanceCache {
		// account -> ccy -> balance
		private val account_map: mutable.Map[String, mutable.Map[String, BigDecimal]] = mutable.LinkedHashMap.empty

		private def add_amount(account: String, ccy: String, n: BigDecimal): Unit = {
			val ccy_map = account_map.getOrElseUpdate(account, mutable.HashMap.empty)
			ccy_map.put(ccy, ccy_map.getOrElse(ccy, zero) + n)
		}
		def set_balance(account: String, ccy: String, n: BigDecimal): Unit = {
			val ccy_map = account_map.getOrElseUpdate(account, mutable.HashMap.empty)
			ccy_map.put(ccy, n)
		}
		def get_opt(account: String, ccy: String): Option[BigDecimal] = {
			account_map.get(account).flatMap(_.get(ccy))
		}
		def list_all_ccy(account: String): Seq[(String, BigDecimal)] = {
			account_map.get(account).map(_.toSeq).getOrElse(Seq.empty)
		}

		def add_trx(t: Trx): Unit = {
			t.postings
				.filter(Accounts.is_assets)
				.foreach(p => add_amount(p.account, p.delta.ccy, p.delta.n))
		}
		def all_balance(): Seq[(String, String, BigDecimal)] = {
			account_map.flatMap { case (account, ccy_map) =>
					ccy_map.map { case (ccy, n) => (account, ccy, n) }
				}
				.filter(_._3 != zero)
				.toSeq
		}
	}

}
