package offline

import bean.entity.{Accounts, Posting, Trx}
import bean.server.BeanDataSource

import java.io.PrintWriter

object ExportFavaCompatible {

	def main(args: Array[String]): Unit = {
		val trx_seq = get_trx_seq()
		val out_file = BeanDataSource.root + "/fava.bean"
		val out = new PrintWriter(out_file)

		fiat_ccy_seq = guess_fiat_ccy(trx_seq)
		write_header(out, trx_seq)
		trx_seq.indices.foreach(i => write_trx(out, trx_seq, i))
		out.close()
		println(s"Exported to $out_file")
	}

	private def get_trx_seq(): Seq[Trx] = {
		val now = System.currentTimeMillis()
		BeanDataSource.a_literals.update(now)
		assert(BeanDataSource.a_literals.data.errors.isEmpty, "has literal errors")

		BeanDataSource.b_balance.update(now)
		assert(BeanDataSource.b_balance.data.isRight, "has balance errors")

		val directives = BeanDataSource.b_balance.data.right.get
		assert(directives.nonEmpty, "empty data")
		directives.flatMap {
			case t: Trx => Some(t)
			case _ => None
		}
	}

	private var fiat_ccy_seq: Set[String] = _
	private def guess_fiat_ccy(trx_seq: Seq[Trx]): Set[String] = {
		val trade_trx_accounts = trx_seq
			.filter(_.postings.exists(_.price.nonEmpty))
			.flatMap(_.postings.map(_.account))
			.toSet
		val non_trade_trx_currencies = trx_seq
			.filterNot(_.postings.exists(p => trade_trx_accounts.contains(p.account)))
			.filterNot(_.postings.exists(p => Accounts.is_income(p.account)))
			.flatMap(_.postings.map(_.delta.ccy))
			.toSet
		non_trade_trx_currencies
	}

	private def write_header(out: PrintWriter, trx_seq: Seq[Trx]): Unit = {
		out.print("2000-01-01 custom \"fava-option\" \"language\" \"zh_CN\"\n")

		// option "operating_currency" "$ccy"
		fiat_ccy_seq.toSeq.sorted
			.foreach(ccy => out.print(s"option \"operating_currency\" \"$ccy\"\n"))

		// $date open $account
		trx_seq.flatMap(t => t.postings.map(p => (p.account, t.date)))
			.groupMapReduce(_._1)(_._2)((x, _) => x)
			.toSeq.sortBy(_._2)
			.foreach { case (a, d) => out.print(s"$d open $a\n") }
	}

	private def write_trx(out: PrintWriter, trx_seq: Seq[Trx], i: Int): Unit = {
		val trx = trx_seq(i)
		out.print(s"${trx.date} * \"${trx.narration}\"\n")

		def write_postings_simple(): Unit = trx.postings.foreach(write_posting_simple(out, _))

		// case: sell
		/*val first_priced = trx.postings.find(_.price.nonEmpty)
		if (first_priced.exists(_.delta.n < 0)) {
			write_postings_simple()
			out.print(s"    Income:Trade\n")
			return
		}*/

		// case: wallet transfer
		/*
		trx.postings.find(_.price.nonEmpty) match {
			case Some(p) if p.delta.n < 0 =>
			// case: sell
			case _ =>
				write_postings_simple()
		}*/
		write_postings_simple()
	}
	private def write_posting_simple(out: PrintWriter, p: Posting): Unit = {
		val suffix = if (fiat_ccy_seq.contains(p.delta.ccy)) "" else " {}"
		write_posting(out, p, suffix)
	}
	private def write_posting(out: PrintWriter, p: Posting, suffix: String): Unit = {
		val n = p.delta.n.bigDecimal.toPlainString
		val ccy = p.delta.ccy
		val space = " " * (40 - p.account.length - n.length).max(1)
		out.print(s"    ${p.account}$space$n $ccy$suffix\n")
	}

}
