package bean.server.data

import bean.entity.Directive
import bean.logic_a.a9_render_literal_err.LiteralErrBlockView
import bean.logic_a.{a1_read_file, a2_parse_literal, a3_parse_directive, a9_render_literal_err}
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.slf4j.LoggerFactory

import java.io.File
import scala.collection.mutable

class LiteralData(root: String) extends ReactiveData[Either[Seq[LiteralErrBlockView], Seq[Directive]]] {
	private val log = LoggerFactory.getLogger(getClass)

	private class FileData(name: String, file: File) extends ReactiveData[Either[Seq[LiteralErrBlockView], Seq[Directive]]] {
		override def update2(now: Long): Unit = {
			val file_modified = file.lastModified()
			if (file_modified + 50 > System.currentTimeMillis()) {
				log.warn(s"file $name just modified, skip reading. (last modified: $file_modified)")
				return
			}
			val latest_hash = file_modified.toString
			if (data_hash == latest_hash) return

			val lines = a1_read_file.read_file(file)
			val a2_either = a2_parse_literal.parse(name, lines)
			val a3_either = a2_either.flatMap(a3_parse_directive.parse)
			data = a3_either.left.map(err => a9_render_literal_err.render(name, err, lines))
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
			data = data_seq.map(_.data).foldLeft(Right(Seq.empty): Either[Seq[LiteralErrBlockView], Seq[Directive]]) {
				case (Left(err1), Left(err2)) => Left(err1 ++ err2)
				case (Left(err), _) => Left(err)
				case (_, Left(err)) => Left(err)
				case (Right(d1), Right(d2)) => Right(d1 ++ d2)
			}.map(a3_parse_directive.sort)
			data_hash = current_hash
		}
	}

}
