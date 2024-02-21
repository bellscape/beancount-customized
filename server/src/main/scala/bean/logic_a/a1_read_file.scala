package bean.logic_a

import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Using

object a1_read_file {


	case class A1_File(name: String, file: File)


	def list(root: String): Seq[A1_File] = {
		val file = new File(root)
		if (!file.isDirectory) return Seq.empty

		val out = ArrayBuffer.empty[A1_File]
		fill_dir(out, file, "")
		out.toSeq.sortBy(_.name)
	}
	private def fill_dir(out: ArrayBuffer[A1_File], file: File, path: String): Unit = {
		for (f <- file.listFiles() if !f.getName.startsWith(".")) {
			if (f.isFile && f.getName.endsWith(".bean")) {
				out += A1_File(path + f.getName, f)
			} else if (f.isDirectory) {
				fill_dir(out, f, path + f.getName + "/")
			}
		}
	}


	def read_file(file: File): Array[String] = {
		Using(Source.fromFile(file)) {_.getLines().toArray}.get
	}

}
