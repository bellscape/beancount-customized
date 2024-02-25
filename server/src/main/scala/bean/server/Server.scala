package bean.server

import io.javalin.Javalin
import io.javalin.websocket.{WsConnectHandler, WsMessageHandler}
import org.slf4j.LoggerFactory

import java.util.concurrent.{Executors, TimeUnit}

object Server {

	def main(args: Array[String]): Unit = {
		val app = Javalin.create()
		init_ws_logging(app)
		init_ws_handlers(app)
		app.start(5002)
		init_schedule()
	}

	private def init_ws_logging(app: Javalin): Unit = {
		app.wsBefore(ws => {
			ws.onConnect(ctx => log.info(s"[ws ${ctx.queryString()}] connect"))
			ws.onMessage(ctx => log.info(s"[ws ${ctx.queryString()}] message: ${ctx.message()}"))
			ws.onClose(ctx => log.info(s"[ws ${ctx.queryString()}] close"))
			ws.onError(ctx => log.error(s"[ws ${ctx.queryString()}] error", ctx.error()))
		})
	}
	private val log = LoggerFactory.getLogger(getClass)

	private def init_ws_handlers(app: Javalin): Unit = {
		app.ws("/ws", ws => {
			ws.onConnect(ctx => {
				val conn = new BeanConnection(ctx)
				ctx.attribute(ATTR_BEAN_CONN, conn)
				BeanController.connections += conn
				conn.init()
			})
			ws.onMessage(ctx => {
				val conn = ctx.attribute(ATTR_BEAN_CONN).asInstanceOf[BeanConnection]
				assert(conn != null, "no connection")
				conn.handle_msg(ctx.message())
			})
			ws.onClose(ctx => {
				val conn = ctx.attribute(ATTR_BEAN_CONN).asInstanceOf[BeanConnection]
				assert(conn != null, "no connection")

				BeanController.connections -= conn
			})
		})
	}
	private val ATTR_BEAN_CONN = "bean"

	private def init_schedule(): Unit = {
		executor.scheduleWithFixedDelay(() => {BeanController.check_data()},
			10, 10, TimeUnit.SECONDS)
	}
	private val executor = Executors.newSingleThreadScheduledExecutor()

}
