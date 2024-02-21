package bean.logic_a

import scala.collection.mutable.ArrayBuffer

object a9_render_literal_err {


	case class LiteralErr(from_i: Int, to_i: Int, hint: String)
	// i: 从 0 开始

	case class LiteralErrLineView(n: Int, text: String, has_err: Boolean, hint: String) {
		def this(n: Int, text: String) = this(n, text, false, "")
		// n: 从 1 开始
	}
	case class LiteralErrBlockView(file: String, lines: ArrayBuffer[LiteralErrLineView] = ArrayBuffer.empty)


	private val CONTEXT_LEN = 2
	private val MAX_GAP = 5

	def render(file: String, errs: Seq[LiteralErr], lines: Array[String]): Seq[LiteralErrBlockView] = {
		val out = ArrayBuffer.empty[LiteralErrBlockView]
		var last_err: LiteralErr = LiteralErr(-100, -100, "")
		for (err <- errs) {

			// step: 判断是否与上个 err 合并
			val should_merge = last_err.hint == err.hint && err.from_i - last_err.to_i - 1 <= MAX_GAP
			last_err = err

			// step: 退回之前的过量输出
			val ctx_from_i = if (should_merge && out.nonEmpty) {
				val last_block = out.last.lines
				while (last_block.nonEmpty && last_block.last.n > err.from_i) {
					last_block.dropRightInPlace(1)
				}
				last_block.last.n
			} else {
				out += LiteralErrBlockView(file)
				(err.from_i - CONTEXT_LEN).max(0)
			}

			// step: 输出 context
			for (i <- ctx_from_i until err.from_i) {
				out.last.lines += new LiteralErrLineView(i + 1, lines(i))
			}
			// step: 输出 from...to
			for (i <- err.from_i to err.to_i) {
				val hint = if (i == err.from_i) err.hint else ""
				out.last.lines += LiteralErrLineView(i + 1, lines(i), true, hint)
			}
			// step: 输出 context
			for (i <- err.to_i + 1 until (err.to_i + CONTEXT_LEN).min(lines.length - 1)) {
				out.last.lines += new LiteralErrLineView(i + 1, lines(i))
			}
		}
		out.toSeq
	}

}
