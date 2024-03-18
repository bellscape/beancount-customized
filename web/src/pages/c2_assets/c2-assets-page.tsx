import { ColumnProps } from 'antd/es/table'
import { observer } from 'mobx-react-lite'
import { Table } from 'antd'
import './c2-assets-page.less'

const antd_columns: ColumnProps<AssetBalanceEntry>[] = [
    { title: 'Account', dataIndex: 'account' },
    { title: 'Amount', render: x => <span className="font-mono">{x.ccy} {x.amount}</span> },
    { title: 'Cost', dataIndex: 'cost', render: x => (x / 10000).toFixed(2) },
    { title: 'Percent', dataIndex: 'percent', render: x => x > 0.01 ? x.toFixed(2) + '%' : '' },
]

interface Props {
    data: C2AssetsPageData
}
const C2AssetsPage = observer(({ data }: Props) => {
    return <div className="c2-assets-page">
        <div className="desc">time: {data.time}</div>
        <Table
            columns={antd_columns}
            rowKey={record => record.account + '/' + record.ccy}
            dataSource={data.assets}
            size="small"
            pagination={false}
            scroll={{ y: 500 }}
        />
    </div>
})
export default C2AssetsPage
