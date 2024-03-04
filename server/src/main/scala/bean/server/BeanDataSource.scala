package bean.server

import bean.entity.Directive
import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.logic_b.b4_render_balance_err.BalanceErrView
import bean.logic_b.{b3_validate_balance, b4_render_balance_err}
import bean.server.data.{LiteralData, ReactiveData}

object BeanDataSource {

	val root = s"${System.getProperty("user.home")}/Documents/bean/data"

	private type A_Result = Either[Seq[LiteralErrBlockView], Seq[Directive]]
	val a_literals: ReactiveData[A_Result] = new LiteralData(root)

	private type B_Result = Either[BalanceErrView, Seq[Directive]]
	val b_balance: ReactiveData[B_Result] = ReactiveData.map[A_Result, B_Result](a_literals, {
		case Right(directives) =>
			val b3_err = b3_validate_balance.validate(directives)
			b4_render_balance_err.render(directives, b3_err)
		case _ => Right(Seq.empty)
	})

}
