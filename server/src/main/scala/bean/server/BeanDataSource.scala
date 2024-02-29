package bean.server

import bean.entity.Directive
import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.server.data.{LiteralData, ReactiveData}

object BeanDataSource {

	val literals: ReactiveData[Either[Seq[LiteralErrBlockView], Seq[Directive]]] = LiteralData

	
	
}
