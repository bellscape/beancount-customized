package bean.server

import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.logic_b.b4_render_balance_err.BalanceErrView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.javalin.websocket.WsContext
import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.LoggerFactory

class BeanConnection(val ctx: WsContext) {
	private val log = LoggerFactory.getLogger(getClass)

	private def ws_send(`type`: String, data: Any): Unit = {
		val msg = mapper.writeValueAsString(Map("type" -> `type`, "data" -> data))
		ctx.send(msg)
		if (`type` != "ping")
			log.info(s"[ws] send: ${msg.take(100)}")
	}
	private val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)


	def init(): Unit = {check_data()}

	private var last_literal_hash = ""
	def check_data(): Unit = {
		try {

			// case: ping
			val now = System.currentTimeMillis()
			BeanDataSource.a_literals.update(now)
			if (BeanDataSource.a_literals.data_hash == last_literal_hash) {
				ws_send("ping", null)
				return
			}
			last_literal_hash = BeanDataSource.a_literals.data_hash

			// case: err.literal
			if (BeanDataSource.a_literals.data.errors.nonEmpty) {
				val a_views: Seq[LiteralErrBlockView] = BeanDataSource.a_literals.data.errors
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
			val time = BeanDataSource.a_literals.data.last_modified
			BeanDataSource.c_assets.update(now)
			val assets = BeanDataSource.c_assets.data
			ws_send("home", Map(
				"time" -> format.format(time),
				"assets" -> assets
			))

		} catch {
			case e: Throwable =>
				log.error("check_data", e)
		}
	}
	private val format = FastDateFormat.getInstance("M-d HH:mm:ss")

	def handle_msg(msg: String): Unit = {
		// ctx.send(s"{handle: $msg}")
	}

}

