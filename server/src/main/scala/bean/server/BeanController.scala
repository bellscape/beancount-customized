package bean.server

import io.javalin.websocket.WsContext

import scala.collection.mutable.ArrayBuffer

class BeanConnection(val ctx: WsContext) {

	def init(): Unit = {
		ctx.send("{init data}")
	}

	def handle_msg(msg: String): Unit = {
		ctx.send(s"{handle: $msg}")
	}

}

object BeanController {
	val connections: ArrayBuffer[BeanConnection] = ArrayBuffer.empty

	private val root = {
		val home = System.getProperty("user.home")
		s"$home/Documents/bean/data"
	}

	def check_data(): Unit = {
	}

}
