package bean.server

import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.logic_b.b4_render_balance_err.BalanceErrView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.javalin.websocket.WsContext
import org.slf4j.LoggerFactory

class BeanConnection(val ctx: WsContext) {
	private val log = LoggerFactory.getLogger(getClass)

	private def ws_send(`type`: String, data: Any): Unit = {
		val msg = mapper.writeValueAsString(Map("type" -> `type`, "data" -> data))
		ctx.send(msg)
		if (`type` != "ping")
			log.info(s"[ws] send: $msg")
	}
	private val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)


	def init(): Unit = {check_data()}

	private var last_literal_hash = ""
	def check_data(): Unit = {

		// case: ping
		val now = System.currentTimeMillis()
		BeanDataSource.a_literals.update(now)
		if (BeanDataSource.a_literals.data_hash == last_literal_hash) {
			ws_send("ping", null)
			return
		}
		last_literal_hash = BeanDataSource.a_literals.data_hash

		// case: err.literal
		if (BeanDataSource.a_literals.data.isLeft) {
			val a_views: Seq[LiteralErrBlockView] = BeanDataSource.a_literals.data.left.get
			ws_send("err.literal", a_views.take(10))
			return
		}

		// case: err.balance
		BeanDataSource.b_balance.update(now)
		if (BeanDataSource.b_balance.data.isLeft) {
			val b_view: BalanceErrView = BeanDataSource.b_balance.data.left.get
			ws_send("err.balance", b_view)
			return
		}

		// case: home
		val directives = BeanDataSource.b_balance.data.getOrElse(Seq.empty)
		ws_send("home", Map(
			"size" -> directives.size,
		))
	}


	def handle_msg(msg: String): Unit = {
		// ctx.send(s"{handle: $msg}")
	}

}

