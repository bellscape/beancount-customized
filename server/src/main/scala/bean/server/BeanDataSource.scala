package bean.server

import bean.entity.Directive
import bean.logic_b.b4_render_balance_err.BalanceErrView
import bean.logic_b.{b3_validate_balance, b4_render_balance_err}
import bean.server.data.{LiteralData, LiteralDataEntry, ReactiveData}

object BeanDataSource {

	val root = s"${System.getProperty("user.home")}/Documents/bean/data"

	val a_literals: ReactiveData[LiteralDataEntry] = new LiteralData(root)

	private type B_Result = Either[BalanceErrView, Seq[Directive]]
	val b_balance: ReactiveData[B_Result] = ReactiveData.map[LiteralDataEntry, B_Result](a_literals, { a_entry =>
		if (a_entry.errors.isEmpty) {
			val b3_err = b3_validate_balance.validate(a_entry.directives)
			b4_render_balance_err.render(a_entry.directives, b3_err)
		} else Right(Seq.empty)
	})

}
