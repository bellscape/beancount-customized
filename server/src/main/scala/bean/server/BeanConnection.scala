package bean.server

import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import io.javalin.websocket.WsContext

class BeanConnection(val ctx: WsContext) {

	private var last_literal_hash = ""

	def init(): Unit = {
		val now = System.currentTimeMillis()
		BeanDataSource.literals.update(now)
		send_beans()
	}

	def handle_msg(msg: String): Unit = {
		// ctx.send(s"{handle: $msg}")
	}

	def check_data(): Unit = {
		val now = System.currentTimeMillis()
		BeanDataSource.literals.update(now)
		if (BeanDataSource.literals.data_hash == last_literal_hash) {
			ctx.send(Map(
				"mode" -> "ping",
			))
			return
		}

		send_beans()
	}

	private def send_beans(): Unit = {
		BeanDataSource.literals.data match {
			case Left(errs: Seq[LiteralErrBlockView]) =>
				ctx.send(Map(
					"mode" -> "literal_err",
					"data" -> errs
				))
			case Right(_) =>
				ctx.send(Map(
					"mode" -> "home",
				))
		}
	}

}

