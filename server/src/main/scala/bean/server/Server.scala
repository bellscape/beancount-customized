package bean.server

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import org.slf4j.LoggerFactory

import java.util.concurrent.{Executors, TimeUnit}

object Server {

	def main(args: Array[String]): Unit = {
		val app = Javalin.create(config => {
			config.staticFiles.add("../web/dist", Location.EXTERNAL)
		})
		init_ws_logging(app)
		init_ws_handlers(app)
		app.start(5002)

		init_schedule()
	}

	private def init_ws_logging(app: Javalin): Unit = {
		app.wsBefore(ws => {
			ws.onConnect(ctx => log.info(s"[ws] connect"))
			// ws.onMessage(ctx => log.info(s"[ws] received: ${ctx.message()}"))
			ws.onClose(ctx => log.info(s"[ws] close"))
			// ws.onError(ctx => log.error(s"[ws] error", ctx.error()))
		})
	}
	private val log = LoggerFactory.getLogger(getClass)

	private var conn: BeanConnection = _
	private def init_ws_handlers(app: Javalin): Unit = {
		app.ws("/ws", ws => {
			ws.onConnect(ctx => {
				if (conn != null)
					conn.ctx.closeSession()
				conn = new BeanConnection(ctx)
				conn.init()
			})
			ws.onMessage(ctx => {
				if (conn != null && conn.ctx.sessionId() == ctx.sessionId())
					conn.handle_msg(ctx.message())
			})
			ws.onClose(ctx => {
				if (conn != null && conn.ctx.sessionId() == ctx.sessionId())
					conn = null
			})
		})
	}

	private def init_schedule(): Unit = {
		executor.scheduleWithFixedDelay(() => {
			if (conn != null)
				conn.refresh_data()
		}, 2, 2, TimeUnit.SECONDS)
	}
	private val executor = Executors.newSingleThreadScheduledExecutor()

}
