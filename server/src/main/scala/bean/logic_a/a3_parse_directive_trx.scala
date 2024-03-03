package bean.logic_a

import bean.entity.{Amount, Posting, Trx}
import bean.logic_a.a2_parse_literal.{ComplexTrxLiteral, PostingLiteral}

object a3_parse_directive_trx {


	private case class InternalPosting(account: String, var delta: Amount,
									   has_price: Boolean, auto_price: Boolean,
									   var price: Option[Amount]) {
		def this(x: PostingLiteral) = this(x.account, x.delta.getOrElse(MISSING_DELTA),
			x.has_price, x.has_price && x.price.isEmpty, x.price)

		def missing_delta: Boolean = delta == MISSING_DELTA

		def build(): Posting = {
			assert(!missing_delta)
			assert(has_price == price.nonEmpty)
			Posting(account, delta, price)
		}
	}
	private val MISSING_DELTA = Amount(0, "missing")

	private case class CtxError(hint: String) extends Exception


	def parse_trx(x: ComplexTrxLiteral): Either[String, Trx] = {
		try {
			val ps = x.postings.toSeq.map(new InternalPosting(_))
			if (ps.size < 2) throw CtxError("至少应有两条过账")

			fix_auto_delta(ps)
			fix_auto_price(ps)
			check_trx_balance(ps)

			val trx = Trx(x.date, ps.map(_.build()), x.narration, x.src)
			Right(trx)
		} catch {
			case e: CtxError => Left(e.hint)
		}
	}


	/* ------------------------- fix: empty delta ------------------------- */

	private def fix_auto_delta(ps: Seq[InternalPosting]): Unit = {
		if (ps.dropRight(1).exists(_.missing_delta)) {
			throw CtxError("只有最后一条过账可省略金额")
		}

		if (ps.last.missing_delta) {
			if (ps.exists(_.has_price)) {
				fix_auto_delta__with_price(ps)
			} else {
				fix_auto_delta__no_price(ps)
			}
		}

		assert(ps.forall(!_.missing_delta))
	}
	private def fix_auto_delta__no_price(ps: Seq[InternalPosting]): Unit = {
		val ccy = ps.head.delta.ccy
		if (ps.dropRight(1).exists(_.delta.ccy != ccy)) {
			throw CtxError("省略金额时，其他过账需同币种")
		}

		ps.last.delta = -Amount.sum(ps.dropRight(1).map(_.delta))
	}
	private def fix_auto_delta__with_price(ps: Seq[InternalPosting]): Unit = {
		// 四条：付款、转入、扣费、支出
		// 四条：卖出、收款、扣费、记费用
		if (ps.size != 4) {
			throw CtxError("有价格 & 省略金额：仅支持四条过账")
		}
		val Seq(p1, p2, p3, p4) = ps
		if (p1.delta.n.sign == p2.delta.n.sign) {
			throw CtxError("带价格过账：前两条方向应相反")
		}
		if (p3.delta.n >= 0) {
			throw CtxError("带价格过账：第三条应为扣费（金额为负）")
		}

		assert(p4.missing_delta)
		p4.delta = -p3.delta
	}


	/* ------------------------- fix: auto price ------------------------- */

	private def fix_auto_price(ps: Seq[InternalPosting]): Unit = {
		val priced_count = ps.count(_.has_price)
		if (priced_count > 1) {
			throw CtxError(s"仅一条过账可标价，现有 $priced_count 条")
		}
		if (!ps.exists(_.auto_price)) {
			return
		}

		val ccy_sums = ps.filterNot(_.has_price)
			.map(_.delta)
			.groupBy(_.ccy)
			.map(x => Amount.sum(x._2))
			.filterNot(_.n == 0)
			.toSeq
		if (ccy_sums.size != 1) {
			val ccy_list = ccy_sums.map(_.ccy).mkString(", ")
			throw CtxError(s"自动标价：无法确定标价币种：$ccy_list")
		}

		val pay = ccy_sums.head
		ps.find(_.has_price).foreach(p => {
			p.price = Some(Amount(-pay.n / p.delta.n, pay.ccy))
		})
		assert(ps.filter(_.has_price)
			.forall(_.price.nonEmpty))
	}


	/* ------------------------- check: trx balance ------------------------- */

	private def check_trx_balance(ps: Seq[InternalPosting]): Unit = {
		// auto price 不检查（可能有精度问题）
		if (ps.exists(_.auto_price)) {
			return
		}

		val ccy_sums = ps
			.map(p => if (p.has_price) {p.price.get * p.delta.n} else {p.delta})
			.groupBy(_.ccy)
			.map(x => Amount.sum(x._2))
			.filterNot(_.n == 0)
			.toSeq
		if (ccy_sums.nonEmpty) {
			val left = ccy_sums.mkString(", ")
			throw CtxError(s"过账金额不平衡：$left")
		}
	}

}
