package bean.logic_c

import bean.entity.{Directive, Trx}
import bean.logic_b.b3_validate_balance.BalanceCache

import java.time.LocalDate

object c2_asset_stat {

	case class AssetBalance(account: String, ccy: String,
							amount: BigDecimal,
							cost: Double,
							var percent: Double = 0)

	def stat(directives: Seq[Directive]): Seq[AssetBalance] = {
		val trx_seq = directives.flatMap {
			case d: Trx => Some(d)
			case _ => None
		}

		val balances = new BalanceCache()
		trx_seq.foreach(balances.add_trx)

		val price = c1_price_db.build_price(LocalDate.now(), trx_seq)

		val assets = balances.all_balance().map { case (account, ccy, n) =>
				AssetBalance(account, ccy, n, n.toDouble * price(ccy))
			}
			.filterNot(_.cost == 0)
			.sortBy(-_.cost)

		val total_cost = assets.map(_.cost).sum
		assets.filter(_.cost > 0).foreach(a => a.percent = a.cost / total_cost * 100)

		assets
	}

}
