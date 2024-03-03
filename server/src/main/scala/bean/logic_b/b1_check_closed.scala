package bean.logic_b

import bean.entity.{AccountClose, Directive, Trx}

import scala.collection.mutable

// close 后不应有 trx
object b1_check_closed {

	// return: false close
	def parse(directives_sorted: Seq[Directive]): Seq[Directive] = {
		val closed = mutable.LinkedHashMap.empty[String, AccountClose]
		val false_closes = mutable.LinkedHashSet.empty[String]
		directives_sorted.foreach {
			case c: AccountClose =>
				if (!closed.contains(c.account))
					closed.put(c.account, c)
			case t: Trx =>
				for (account <- t.postings.map(_.account)) {
					if (closed.contains(account))
						false_closes.add(account)
				}
			case _ =>
		}
		false_closes.toSeq.flatMap(closed.get)
	}

}
