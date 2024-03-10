package util

import java.io.{BufferedWriter, File, FileWriter}
import scala.io.Source
import scala.util.Using

object FileUtil {

	def read_lines(file: File): Array[String] = {
		Using(Source.fromFile(file)) {_.getLines().toArray}.get
	}
	def write_lines(file: File, f: (String => Unit) => Unit): Unit = {
		Using(new BufferedWriter(new FileWriter(file))) { out =>
			f.apply(line => {
				out.write(line)
				out.newLine()
			})
		}
	}

}
