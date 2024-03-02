import { observer } from 'mobx-react-lite'
import HomePageModel from './home-page-model.ts'
import LiteralErrPage from './literal_err/literal-err-page.tsx'


const home = new HomePageModel()

const HomePage = observer(() => {
    console.log('home.page_type', home.page_type)
    if (home.page_type === 'literal_err') {
        return LiteralErrPage(home.page_data)
    }
    if (home.page_type === 'home') {
        return <div>Home</div>
    }
    return <div>loading...</div>
})

export default HomePage
