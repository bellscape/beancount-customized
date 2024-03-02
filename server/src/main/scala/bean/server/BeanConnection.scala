package bean.server

import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.javalin.websocket.WsContext

class BeanConnection(val ctx: WsContext) {

	private def ws_send(`type`: String, data: Any): Unit = {
		val msg = Map("type" -> `type`, "data" -> data)
		ctx.send(mapper.writeValueAsString(msg))
	}
	private val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)


	def init(): Unit = {check_data()}

	private var last_literal_hash = ""
	def check_data(): Unit = {
		val now = System.currentTimeMillis()
		BeanDataSource.literals.update(now)
		if (BeanDataSource.literals.data_hash == last_literal_hash) {
			ws_send("ping", null)
			return
		}
		last_literal_hash = BeanDataSource.literals.data_hash


		BeanDataSource.literals.data match {
			case Left(errs: Seq[LiteralErrBlockView]) =>
				ws_send("literal_err", errs.take(10))
			case Right(directives) =>
				ws_send("home", Map(
					"size" -> directives.size,
				))
		}
	}


	def handle_msg(msg: String): Unit = {
		// ctx.send(s"{handle: $msg}")
	}

}

