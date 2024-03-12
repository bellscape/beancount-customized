package bean.logic_c

import bean.entity.Trx

import java.time.LocalDate
import java.util

object c1_price_db {

	def build_price(day: LocalDate, trx_seq: Seq[Trx]): String => Double = {
		lazy val gbp_to_cny = c1_price_db__yahoo.build_reader("GBPCNY=X")(day)
		lazy val usd_to_cny = c1_price_db__yahoo.build_reader("CNY=X")(day)
		lazy val hkd_to_cny = c1_price_db__yahoo.build_reader("HKDCNY=X")(day)
		lazy val twd_to_usd = c1_price_db__yahoo.build_reader("TWD=X")(day)
		lazy val btc_to_usd = c1_price_db__yahoo.build_reader("BTC-USD")(day)
		lazy val eth_to_usd = c1_price_db__yahoo.build_reader("ETH-USD")(day)

		val cache: util.HashMap[String, Double] = new util.HashMap[String, Double]()

		/*		val latest_price: Map[String, Amount] = trx_seq.filterNot(_.date.isAfter(day))
					.flatMap(_.postings)
					.filter(_.price.nonEmpty)
					.groupMapReduce(_.delta.ccy)(_.price.get)((_, b) => b)
		*/
		def get_price(ccy: String): Double = ccy match {
			case "CNY" => 1.0
			case "USD" => usd_to_cny
			case "GBP" => gbp_to_cny
			case "HKD" => hkd_to_cny
			case "TWD" => twd_to_usd / usd_to_cny
			case "BTC" => btc_to_usd * usd_to_cny
			case "ETH" => eth_to_usd * usd_to_cny
			case _ =>
				cache.computeIfAbsent(ccy, _ => {
					val price_opt = trx_seq.filterNot(_.date.isAfter(day))
						.flatMap(_.postings)
						.filter(_.delta.ccy == ccy)
						.flatMap(_.price)
						.lastOption
					price_opt match {
						case Some(price) => price.n.doubleValue * get_price(price.ccy)
						case _ =>
							println(s"Unknown currency: $ccy")
							???
					}
				})
		}
		get_price
	}

}
