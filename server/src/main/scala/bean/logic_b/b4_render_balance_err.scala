package bean.logic_b

import bean.entity.{AccountClose, Balance, Directive, Trx}
import bean.logic_b.b3_validate_balance.BalanceErr

import java.time.LocalDate
import scala.collection.mutable.ArrayBuffer

object b4_render_balance_err {

	case class BalanceErrView(
								 desc: String,
								 journal_account: String,
								 journal_ccy: String,
								 journals: Seq[JournalEntry],
							 )

	def render(directives: Seq[Directive], b3_err: Seq[BalanceErr]): Either[BalanceErrView, Seq[Directive]] = {
		if (b3_err.isEmpty) return Right(directives)

		val desc = render_desc(b3_err)

		val first_err = b3_err.head
		val journal_account = first_err.account
		val journal_ccy = first_err.expect_amount.ccy
		val journals = render_journals(directives, journal_account, journal_ccy)

		Left(BalanceErrView(desc, journal_account, journal_ccy, journals))
	}

	private def render_desc(errors: Seq[BalanceErr]): String = {
		errors.groupBy(e => (e.account, e.expect_amount.ccy)).toSeq
			.map { case ((account, ccy), es) =>
				val desc_arr = es.map(e => s"${e.date}(${e.missing.str_signed} -> ${e.expect_amount.n})")
				s"$account/$ccy: ${desc_arr.mkString(", ")}"
			}.mkString("\n")
	}

	case class JournalEntry(
							   balance: String, delta: String, date: String,
							   comment: String, src: String,
							   is_trx: Boolean,
							   var is_suspicious: Boolean = false,
							   var is_first_in_date: Boolean = false,
							   var is_last_in_date: Boolean = false
						   )

	private def render_journals(directives: Seq[Directive], account: String, ccy: String): Seq[JournalEntry] = {
		val out = ArrayBuffer.empty[JournalEntry]

		var allow_init = true
		var balance = BigDecimal(0)
		val out_buf = ArrayBuffer.empty[JournalEntry]

		def check_balance(expect: BigDecimal, date: LocalDate, directive_type: String, directive_src: String): Unit = {
			val missing = expect - balance
			val is_suspicious = missing != 0
			if (is_suspicious) {
				out_buf.foreach(_.is_suspicious = true)
				out ++= out_buf
			}
			out_buf.clear()

			val entry = JournalEntry(
				balance.toString(), missing.str_signed, date.toString,
				s"<$directive_type>", directive_src,
				is_trx = false, is_suspicious)
			out += entry
		}

		directives.foreach {
			case t: Trx =>
				val postings = t.postings.filter(p => p.account == account && p.delta.ccy == ccy)
				if (postings.nonEmpty) {
					allow_init = false
					val delta = postings.map(_.delta.n).sum
					balance += delta

					val entry = JournalEntry(
						balance.toString(), delta.str_signed, t.date.toString,
						build_trx_comment(t, account, ccy), t.src.toString, is_trx = true)
					out_buf += entry
				}
			case b: Balance =>
				if (b.account == account && b.amount.ccy == ccy) {
					if (allow_init) {
						allow_init = false
						balance = b.amount.n
					}
					check_balance(b.amount.n, b.date, "balance", b.src.toString)
					balance = b.amount.n
				}
			case c: AccountClose =>
				if (c.account == account) {
					check_balance(0, c.date, "close", c.src.toString)
					balance = 0
				}
		}
		out ++= out_buf

		// fix: is_first_in_date, is_last_in_date
		for ((entry, i) <- out.zipWithIndex.drop(1)) {
			if (entry.date != out(i - 1).date) {
				out(i - 1).is_last_in_date = true
				entry.is_first_in_date = true
			}
		}

		out.toSeq
	}
	private def build_trx_comment(t: Trx, account: String, ccy: String): String = {
		val out = new StringBuilder()

		val related = t.postings.map(p => (p.account, p.delta.ccy))
			.map { case (a, c) => (if (a == account) "" else a, if (c == ccy) "" else c) }
			.filter { case (a, c) => a.nonEmpty || c.nonEmpty }
			.sorted.distinct
			.map { case (a, c) => if (a.isEmpty) c else if (c.isEmpty) a else s"$a/$c" }
			.mkString(", ")
		out ++= related

		if (t.narration.nonEmpty)
			out ++= s" // ${t.narration}"

		out.toString()
	}

	implicit class RichBigDecimal(raw: BigDecimal) {
		def str_signed: String = if (raw > 0) s"+$raw" else raw.toString
	}

}
