package bean.logic_a.entity

import scala.collection.mutable.ArrayBuffer


case class LiteralErr(from: Int, to: Int, hint: String)


case class LiteralErrLineView(n: Int, text: String, has_err: Boolean, hint: String) {
	def this(n: Int, text: String) = this(n, text, false, "")
}
case class LiteralErrBlockView(file: String, lines: ArrayBuffer[LiteralErrLineView] = ArrayBuffer.empty)
