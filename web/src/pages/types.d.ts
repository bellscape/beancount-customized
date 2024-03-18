// a_literal_err

type LiteralErrLine = {
    n: number
    text: string
    has_err: boolean
    hint: string
}
type LiteralErrBlock = {
    label: string
    lines: LiteralErrLine[]
}

// b_balance_err

type BalanceErrData = {
    desc: string
    journal_account: string
    journal_ccy: string
    journals: JournalEntry[]
}
// {"balance":"4.55","delta":"+4.55","date":"2023-12-21","narration":"利息","is_trx":true,"is_suspicious":true,"is_first_in_date":true,"is_last_in_date":true}
type JournalEntry = {
    balance: string
    delta: string
    date: string
    comment: string
    src: string
    is_trx: boolean
    is_suspicious: boolean
    is_first_in_date: boolean
    is_last_in_date: boolean
}

// c2_assets

type AssetBalanceEntry = {
    account: string
    ccy: string
    amount: string
    cost: number,
    percent: number
}
type C2AssetsPageData = {
    time: string
    assets: AssetBalanceEntry[]
}
