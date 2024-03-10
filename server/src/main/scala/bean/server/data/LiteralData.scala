package bean.server.data

import bean.entity.Directive
import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.logic_a.{a1_read_file, a2_parse_literal, a3_parse_directive, a9_render_literal_err}
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.slf4j.LoggerFactory
import util.FileUtil

import java.io.File
import scala.collection.mutable

case class LiteralDataEntry(last_modified: Long,
							directives: Seq[Directive],
							errors: Seq[LiteralErrBlockView])

final class LiteralData(root: String) extends ReactiveData[LiteralDataEntry] {
	private val log = LoggerFactory.getLogger(getClass)

	private class FileData(name: String, file: File) extends ReactiveData[LiteralDataEntry] {
		override def update2(now: Long): Unit = {
			val file_modified = file.lastModified()
			if (file_modified + 50 > System.currentTimeMillis()) {
				log.warn(s"file $name just modified, skip reading. (last modified: $file_modified)")
				return
			}
			val latest_hash = file_modified.toString
			if (data_hash == latest_hash) return

			val lines = FileUtil.read_lines(file)
			val (a2_out, a2_err) = a2_parse_literal.parse(name, lines)
			val (a3_out, a3_err) = a3_parse_directive.parse(a2_out)
			val first_err = if (a2_err.nonEmpty) a2_err else a3_err
			val err_views = a9_render_literal_err.render(name, first_err, lines)

			data = LiteralDataEntry(file_modified, a3_out, err_views)
			data_hash = latest_hash
		}
	}

	private val cache: mutable.HashMap[String, FileData] = mutable.HashMap.empty

	override protected def update2(now: Long): Unit = {
		val file_seq = a1_read_file.list(root)

		val valid_keys = file_seq.map(_.name).toSet
		(cache.keySet -- valid_keys).toSeq.foreach(cache.remove)

		val data_seq = file_seq.map { file =>
			cache.getOrElseUpdate(file.name, new FileData(file.name, file.file))
		}
		data_seq.foreach(_.update(now))

		val current_hash = data_seq.map(_.data_hash).foldLeft(new HashCodeBuilder())((b, h) => b.append(h)).toHashCode.toString
		// println(s"debug hash: $current_hash // ${data_seq.map(_.data_hash)}")
		if (data_hash != current_hash) {
			val batch = data_seq.map(_.data)
			val last_modified = batch.map(_.last_modified).maxOption.getOrElse(0L)
			val directives = batch.flatMap(_.directives)
			val directives_sorted = a3_parse_directive.sort(directives)
			val errors = batch.flatMap(_.errors)
			data = LiteralDataEntry(last_modified, directives_sorted, errors)
			data_hash = current_hash
		}
	}

}
