package bean.logic_c

import bean.server.BeanDataSource
import util.FileUtil

import java.io.File
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}
import java.time.{Duration, LocalDate}
import scala.jdk.CollectionConverters.CollectionHasAsScala

object c1_price_db__yahoo {

	/* ------------------------- reader ------------------------- */

	def build_reader(label: String): LocalDate => Double = {
		val lines = FileUtil.read_lines(get_file(label))
			.filter(_.nonEmpty)
			.filterNot(_.contains(",null,"))
		assert(lines.length > 1)
		assert(lines.head == "Date,Open,High,Low,Close,Adj Close,Volume")

		val (first_day, first_day_open) = {
			val cells = lines(1).split(",")
			(LocalDate.parse(cells(0)), cells(1).toDouble)
		}
		val (last_day, last_day_close) = {
			val cells = lines.last.split(",")
			(LocalDate.parse(cells(0)), cells(4).toDouble)
		}
		val day_mid: Map[LocalDate, Double] = lines.drop(1).map(line => {
			val cells = line.split(",")
			val date = LocalDate.parse(cells(0))
			val open = cells(1).toDouble
			val close = cells(4).toDouble
			val price = (open + close) / 2
			(date, price)
		}).toMap

		day => {
			if (day.isBefore(first_day)) {
				first_day_open
			} else if (day.isAfter(last_day)) {
				last_day_close
			} else {
				// 无数据时向前继续查
				var cursor = day
				while (!day_mid.contains(cursor)) {
					cursor = cursor.minusDays(1)
				}
				day_mid(cursor)
			}
		}
	}

	/* ------------------------- prepare ------------------------- */

	def prepare_price(label: String, first_day: String): Unit = {
		val file = get_file(label)
		val prev_lines = if (file.isFile) FileUtil.read_lines(file) else Array.empty[String]

		val fetch_first_day = if (prev_lines.length > 1) {
			LocalDate.parse(prev_lines.last.split(",", 2).head).plusDays(1)
		} else {
			LocalDate.parse(first_day)
		}
		val fetch_last_day = LocalDate.now().minusDays(1)
		if (fetch_first_day.isAfter(fetch_last_day)) {
			println(s"[yahoo/$label] already up to date")
			return
		}
		val new_lines = http_get(label, fetch_first_day, fetch_last_day)
		if (new_lines.length < 2) {
			println(s"[yahoo/$label] no new data")
			return
		}

		FileUtil.write_lines(file, print => {
			if (prev_lines.nonEmpty) {
				prev_lines.foreach(print)
				new_lines.drop(1).foreach(print)
			} else {
				new_lines.foreach(print)
			}
		})
		println(s"[yahoo/$label] saved until $fetch_last_day")
	}

	private def http_get(label: String, first_day: LocalDate, last_day: LocalDate): Array[String] = {
		// https://query1.finance.yahoo.com/v7/finance/download/BTC-USD?period1=1709596800&period2=1710028800&interval=1d&events=history&includeAdjustedClose=true
		val from_s = first_day.toEpochDay * 24 * 3600
		val to_s = last_day.toEpochDay * 24 * 3600
		val url = s"https://query1.finance.yahoo.com/v7/finance/download/$label?period1=$from_s&period2=$to_s&interval=1d&events=history&includeAdjustedClose=true"
		println("fetching: " + url)
		val req = HttpRequest.newBuilder()
			.uri(new java.net.URI(url))
			.GET()
			.build()
		val resp = http_client.send(req, BodyHandlers.ofString())
		assert(resp.statusCode() == 200)
		resp.body().lines.toList.asScala.toArray
	}
	private lazy val http_client = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(10))
		.build()

	private def get_file(label: String): File = {
		new File(s"${BeanDataSource.root}/price/yahoo-$label.csv")
	}

	/* ------------------------- main ------------------------- */

	def main(args: Array[String]): Unit = {
		// prepare_price("BTC-USD", "2015-01-01")
		// prepare_price("CNY=X", "2018-01-01")
		// prepare_price("GBPCNY=X", "2018-01-01")
		// prepare_price("HKDCNY=X", "2018-01-01")
	}

}
