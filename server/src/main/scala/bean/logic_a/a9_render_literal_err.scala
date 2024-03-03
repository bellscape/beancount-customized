package bean.logic_a

import bean.entity.Src

import scala.collection.mutable.ArrayBuffer

object a9_render_literal_err {

	// n: 从 1 开始
	case class LiteralErr(first_n: Int, last_n: Int, hint: String) {
		def this(src: Src, hint: String) = this(src.n, src.n, hint)
	}

	case class LiteralErrLineView(n: Int, text: String, has_err: Boolean, hint: String) {
		def this(n: Int, text: String) = this(n, text, false, "")
		// n: 从 1 开始
	}
	case class LiteralErrBlockView(label: String, lines: ArrayBuffer[LiteralErrLineView] = ArrayBuffer.empty)


	private val CONTEXT_LEN = 2
	private val MAX_GAP = 5

	def render(file: String, errs: Seq[LiteralErr], lines: Array[String]): Seq[LiteralErrBlockView] = {
		val out = ArrayBuffer.empty[LiteralErrBlockView]
		var last_err: LiteralErr = LiteralErr(-100, -100, "")
		for (err <- errs) {

			// step: 判断是否与上个 err 合并
			val should_merge = last_err.hint == err.hint && err.first_n - last_err.last_n - 1 <= MAX_GAP
			last_err = err

			// step: 退回之前的过量输出
			val ctx_from_n = if (should_merge && out.nonEmpty) {
				val last_block = out.last.lines
				while (last_block.nonEmpty && last_block.last.n >= err.first_n) {
					last_block.dropRightInPlace(1)
				}
				last_block.last.n + 1
			} else {
				out += LiteralErrBlockView(file)
				(err.first_n - CONTEXT_LEN).max(1)
			}

			// step: 输出 context
			for (n <- ctx_from_n until err.first_n) {
				out.last.lines += new LiteralErrLineView(n, lines(n - 1))
			}
			// step: 输出 from...to
			for (n <- err.first_n to err.last_n) {
				val hint = if (n == err.first_n) err.hint else ""
				out.last.lines += LiteralErrLineView(n, lines(n - 1), has_err = true, hint)
			}
			// step: 输出 context
			for (n <- err.last_n + 1 to (err.last_n + CONTEXT_LEN).min(lines.length)) {
				out.last.lines += new LiteralErrLineView(n, lines(n - 1))
			}
		}
		out.toSeq
	}

}
