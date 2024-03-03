package bean.logic_a

import bean.entity.{Amount, Src}
import bean.logic_a.a9_render_literal_err.LiteralErr
import org.apache.commons.lang3.StringUtils.trimToEmpty

import java.time.LocalDate
import scala.collection.mutable.ArrayBuffer

object a2_parse_literal {


	trait Literal extends _ParseLineResult

	case class BalanceLiteral(date: LocalDate, account: String, amount: Amount, src: Src) extends Literal
	case class CloseLiteral(date: LocalDate, account: String, src: Src) extends Literal
	case class SimpleTrxLiteral(date: LocalDate, from_account: String, to_account: String,
								amount: Amount, narration: String, src: Src) extends Literal
	case class ComplexTrxLiteral(date: LocalDate, narration: String,
								 src: Src, var last_n: Int,
								 postings: ArrayBuffer[PostingLiteral] = ArrayBuffer.empty) extends Literal
	case class PostingLiteral(account: String, delta: Option[Amount], has_price: Boolean, price: Option[Amount]) extends _ParseLineResult


	def parse(file: String, lines: Array[String]): Either[Seq[LiteralErr], Seq[Literal]] = {
		val out = ArrayBuffer.empty[Literal]
		val errs = ArrayBuffer.empty[LiteralErr]
		for ((line, i) <- lines.zipWithIndex) {
			val src = Src(file, i + 1)
			parse_line(line, src) match {
				case EmptyLine =>
				case IllegalLine =>
					errs += new LiteralErr(src, "本行格式不对")
				case p: PostingLiteral =>
					out.lastOption match {
						case Some(t: ComplexTrxLiteral) =>
							t.postings += p
							t.last_n = src.n
						case _ =>
							errs += new LiteralErr(src, "过账应紧随交易")
					}
				case l: Literal =>
					out += l
			}
		}
		if (errs.isEmpty) Right(out.toSeq) else Left(errs.toSeq)
	}


	sealed trait _ParseLineResult
	private object EmptyLine extends _ParseLineResult
	private object IllegalLine extends _ParseLineResult

	private def parse_line(line: String, src: Src): _ParseLineResult = {
		trim_comments(line) match {
			// 此处 match 顺序考虑性能

			case "" => EmptyLine

			case CUSTOM_TRX_LINE(date, from_account, to_account, amount, ccy, _, narration) =>
				SimpleTrxLiteral(LocalDate.parse(date),
					from_account, to_account,
					Amount(BigDecimal(amount), ccy),
					trimToEmpty(narration), src)

			case BALANCE_LINE(date, account, amount, ccy) =>
				BalanceLiteral(LocalDate.parse(date), account,
					Amount(BigDecimal(amount), ccy), src)
			case CLOSE_LINE(date, account) =>
				CloseLiteral(LocalDate.parse(date), account, src)

			case TRX_HEADER_LINE(date, _, narration) =>
				ComplexTrxLiteral(LocalDate.parse(date),
					trimToEmpty(narration), src, src.n)

			case POSTING_LINE__NO_AMOUNT(account) =>
				PostingLiteral(account, delta = None,
					has_price = false, price = None)
			case POSTING_LINE__REGULAR(account, amount, ccy) =>
				PostingLiteral(account, delta = Some(Amount(BigDecimal(amount), ccy)),
					has_price = false, price = None)
			case POSTING_LINE__WITH_PRICE(account, amount, ccy, price, price_ccy) =>
				PostingLiteral(account, delta = Some(Amount(BigDecimal(amount), ccy)),
					has_price = true, price = Some(Amount(BigDecimal(price), price_ccy)))
			case POSTING_LINE__AUTO_PRICE(account, amount, ccy) =>
				PostingLiteral(account, delta = Some(Amount(BigDecimal(amount), ccy)),
					has_price = true, price = None)

			case _ => IllegalLine
		}
	}

	// 2022-02-01 balance Binance   100 USD
	private val BALANCE_LINE = """(\d{4}-\d{2}-\d{2}) balance (\S+)\s+([-\d.]+) (\S+)""".r
	// 2022-02-01 close Assets:Binance
	private val CLOSE_LINE = """(\d{4}-\d{2}-\d{2}) close (\S+)""".r
	// 2021-08-24 custom Assets:Revolut Expenses:Daily   5.00 GBP "Sure"
	private val CUSTOM_TRX_LINE = """(\d{4}-\d{2}-\d{2}) custom (\S+) (\S+)\s+([-\d.]+) (\S+)(\s+"([^"]*)")?""".r
	// 2022-02-01 * "利息"
	private val TRX_HEADER_LINE = """(\d{4}-\d{2}-\d{2}) \*(\s+"([^"]*)")?""".r
	//     Expenses:Fee
	private val POSTING_LINE__NO_AMOUNT = """\s+(\S+)""".r
	//     Assets:Binance   100 USD
	private val POSTING_LINE__REGULAR = """\s+(\S+)\s+([-\d.]+) (\S+)""".r
	//     Assets:Binance   0.01 BTC { 20000 USD }
	private val POSTING_LINE__WITH_PRICE = """\s+(\S+)\s+([-\d.]+) (\S+) \{ ([-\d.]+) (\S+) }""".r
	//     Assets:Trading212   0.4421038 MSFT {}
	private val POSTING_LINE__AUTO_PRICE = """\s+(\S+)\s+([-\d.]+) (\S+) \{\s*}""".r

	private def trim_comments(s: String): String = {
		val no_comments = s.indexOf(';') match {
			case -1 => s
			case i => s.substring(0, i)
		}
		no_comments.replaceAll("\\s+$", "")
	}

}
