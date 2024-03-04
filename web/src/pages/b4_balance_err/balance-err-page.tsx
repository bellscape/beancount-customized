import { observer } from 'mobx-react-lite'
import { useState } from 'react'
import { Checkbox, Table } from 'antd'
import { ColumnProps } from 'antd/es/table'
import './balance-err-page.less'


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

const antd_columns: ColumnProps<JournalEntry>[] = [
    { title: 'Balance', dataIndex: 'balance', className: 'is-mono', align: 'right' },
    { title: 'Delta', dataIndex: 'delta', className: 'is-mono' },
    { title: 'Date', dataIndex: 'date', className: 'is-mono' },
    { title: 'Comment', dataIndex: 'comment' },
    { title: 'Src', dataIndex: 'src' },
]
function row_cls(record: JournalEntry): string {
    let cls = record.is_trx ? 'is-trx' : 'is-check'
    if (record.is_suspicious) cls += ' is-suspicious'
    if (record.is_first_in_date) cls += ' is-first-in-date'
    if (record.is_last_in_date) cls += ' is-last-in-date'
    return cls
}

interface Props {
    data: BalanceErrData
}
const BalanceErrPage = observer(({ data }: Props) => {
    const [reversed, setReversed] = useState(false)
    return <div className="balance-err-page">
        <h1>Balance Error</h1>
        <div className="desc">{data.desc}</div>
        <div className="table-desc">{data.journal_account}/{data.journal_ccy}</div>
        <div><Checkbox checked={reversed} onChange={e => setReversed(e.target.checked)}>reversed</Checkbox></div>
        <Table
            columns={antd_columns}
            rowKey="src"
            rowClassName={row_cls}
            dataSource={reversed ? data.journals.slice().reverse() : data.journals}
            size="small"
            pagination={false}
        />
    </div>
})

export default BalanceErrPage
