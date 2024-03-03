package bean.entity

object Accounts {

	def is_assets(a: String): Boolean = a.startsWith("Assets:")
	def is_income(a: String): Boolean = a.startsWith("Income:")
	def is_expenses(a: String): Boolean = a.startsWith("Expenses:")

	def is_assets(p: Posting): Boolean = is_assets(p.account)

}
