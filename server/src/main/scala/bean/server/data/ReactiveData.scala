package bean.server.data

abstract class ReactiveData[T] {
	var data: T = _
	var data_hash: String = ""
	def update_and_get(now: Long): T = {
		update(now)
		data
	}

	private var last_check: Long = 0
	final def update(now: Long): Unit = {
		if (last_check != now) {
			last_check = now
			update2(now)
		}
	}
	protected def update2(now: Long): Unit
}

object ReactiveData {

	def map[A, B](a: ReactiveData[A], f: A => B): ReactiveData[B] = {
		new ReactiveData[B] {
			override def update2(now: Long): Unit = {
				a.update(now)
				if (data_hash != a.data_hash) {
					data = f(a.data)
					data_hash = a.data_hash
				}
			}
		}
	}

}