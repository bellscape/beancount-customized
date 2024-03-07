package bean.logic_a

import bean.entity._
import bean.logic_a.a2_parse_literal._
import bean.logic_a.a9_render_literal_err.LiteralErr

import scala.collection.mutable.ArrayBuffer

object a3_parse_directive {

	def parse(literals: Seq[Literal]): (Seq[Directive], Seq[LiteralErr]) = {
		val out = ArrayBuffer.empty[Directive]
		val errs = ArrayBuffer.empty[LiteralErr]
		literals.foreach {
			case t: SimpleTrxLiteral => out += translate_simple_trx(t)
			case t: BalanceLiteral => out += translate_balance(t)
			case t: CloseLiteral => out += translate_close(t)
			case t: ComplexTrxLiteral =>
				a3_parse_directive_trx.parse_trx(t) match {
					case Left(hint) => errs += LiteralErr(t.src.n, t.last_n, hint)
					case Right(trx) => out += trx
				}
		}
		(out.toSeq, errs.toSeq)
	}
	private def translate_balance(x: BalanceLiteral): Balance = {
		Balance(x.date, x.account, x.amount, x.src)
	}
	private def translate_close(x: CloseLiteral): AccountClose = {
		AccountClose(x.date, x.account, x.src)
	}
	private def translate_simple_trx(x: SimpleTrxLiteral): Directive = {
		val p_from = Posting(x.from_account, -x.amount, None)
		val p_to = Posting(x.to_account, x.amount, None)
		Trx(x.date, Seq(p_from, p_to), x.narration, x.src)
	}

	def sort(directives: Seq[Directive]): Seq[Directive] = {
		directives.sortBy {
			case x: Trx => (x.date, 1)
			case x => (x.date, 0)
		}
	}

}
