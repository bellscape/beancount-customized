package bean.logic_a

import bean.logic_a.entity.{LiteralErr, LiteralErrBlockView, LiteralErrLineView}

import scala.collection.mutable.ArrayBuffer

object a9_render_literal_err {

	private val CONTEXT_LEN = 2
	private val MAX_GAP = 5

	def render(file: String, errs: Seq[LiteralErr], lines: Array[String]): Seq[LiteralErrBlockView] = {
		val out = ArrayBuffer.empty[LiteralErrBlockView]
		var last_err: LiteralErr = LiteralErr(-100, -100, "")
		for (err <- errs) {

			// step: 判断是否与上个 err 合并
			val should_merge = last_err.hint == err.hint && err.from - last_err.to - 1 <= MAX_GAP
			last_err = err

			// step: 退回之前的过量输出
			val ctx_from = if (should_merge && out.nonEmpty) {
				val last_block_lines = out.last.lines
				var last_n = last_block_lines.last.n
				while (last_n >= err.from) {
					last_n -= 1
					last_block_lines.dropRightInPlace(1)
				}
				last_n + 1
			} else {
				out += LiteralErrBlockView(file)
				Math.max(1, err.from - CONTEXT_LEN)
			}

			// step: 输出 context
			for (i <- ctx_from until err.from) {
				out.last.lines += new LiteralErrLineView(i, lines(i - 1))
			}
			// step: 输出 from...to
			for (i <- err.from to err.to) {
				val hint = if (i == err.from) err.hint else ""
				out.last.lines += LiteralErrLineView(i, lines(i - 1), true, hint)
			}
			// step: 输出 context
			for (i <- err.to + 1 until Math.min(err.to + CONTEXT_LEN, lines.length)) {
				out.last.lines += new LiteralErrLineView(i, lines(i - 1))
			}
		}
		out.toSeq
	}

}
