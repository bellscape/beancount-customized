package bean.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.javalin.websocket.WsContext
import org.apache.commons.lang3.time.FastDateFormat
import org.slf4j.LoggerFactory

class BeanConnection(val ctx: WsContext) {
	private val log = LoggerFactory.getLogger(getClass)

	private def ws_send(page: String, data: Any): Unit = {
		val msg = mapper.writeValueAsString(Map("page" -> page, "data" -> data))
		ctx.send(msg)
		if (page.nonEmpty)
			log.info(s"[ws] send: ${msg.take(100)}")
	}
	private val mapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)


	def init(): Unit = {refresh_data()}

	private var last_literal_hash = ""
	def refresh_data(): Unit = {
		try {
			val (page, data) = do_refresh_data()
			ws_send(page, data)
		} catch {
			case e: Throwable =>
				log.error("refresh_data", e)
		}
	}

	def handle_msg(msg: String): Unit = {
		// ctx.send(s"{handle: $msg}")
	}

	/* ------------------------- resp ------------------------- */

	// return (page, data)
	def do_refresh_data(): (String, Any) = {
		import BeanDataSource._

		// case: ping
		val now = System.currentTimeMillis()
		a_literals.update(now)
		if (last_literal_hash == a_literals.data_hash)
			return ("", null)
		last_literal_hash = a_literals.data_hash

		// case: err.literal
		if (a_literals.data.errors.nonEmpty)
			return ("err-literal", a_literals.data.errors.take(10))

		// case: err.balance
		b_balance.update(now)
		if (b_balance.data.isLeft)
			return ("err-balance", b_balance.data.left.get)

		val last_modified = format.format(a_literals.data.last_modified)

		// case: c2_assets
		c2_assets.update(now)
		val assets = c2_assets.data
		("c2_assets", Map(
			"time" -> last_modified,
			"assets" -> assets
		))
	}
	private val format = FastDateFormat.getInstance("M-d HH:mm:ss")

}

