package bean.logic_c

import bean.entity.{Amount, Trx}

import java.time.LocalDate

object c1_price_db {

	def build_price(day: LocalDate, trx_seq: Seq[Trx]): String => Double = {
		val gbp_to_cny = c1_price_db__yahoo.build_reader("GBPCNY=X")(day)
		val usd_to_cny = c1_price_db__yahoo.build_reader("CNY=X")(day)
		val hkd_to_cny = c1_price_db__yahoo.build_reader("HKDCNY=X")(day)
		val btc_to_usd = c1_price_db__yahoo.build_reader("BTC-USD")(day)

		val latest_price: Map[String, Amount] = trx_seq.filterNot(_.date.isAfter(day))
			.flatMap(_.postings)
			.filter(_.price.nonEmpty)
			.groupMapReduce(_.delta.ccy)(_.price.get)((_, b) => b)

		def get_price(ccy: String): Double = ccy match {
			case "CCY" => 1.0
			case "USD" => usd_to_cny
			case "GBP" => gbp_to_cny
			case "HKD" => hkd_to_cny
			case "BTC" => btc_to_usd * usd_to_cny
			case _ =>
				val price = latest_price(ccy)
				price.n.doubleValue * get_price(price.ccy)
		}
		get_price
	}

}
