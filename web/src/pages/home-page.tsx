import { observer } from 'mobx-react-lite'
import HomePageModel from './home-page-model.ts'
import LiteralErrPage from './a9_literal_err/literal-err-page.tsx'
import BalanceErrPage from './b4_balance_err/balance-err-page.tsx'


const home = new HomePageModel()

const HomePage = observer(() => {
    console.log('home.page_type', home.page_type)
    if (home.page_type === 'err.literal') {
        return <LiteralErrPage blocks={home.page_data} />
    }
    if (home.page_type === 'err.balance') {
        return <BalanceErrPage data={home.page_data} />
    }
    if (home.page_type === 'home') {
        return <div>OK</div>
    }
    return <div>loading...</div>
})

export default HomePage
