import classNames from 'classnames'
import { observer } from 'mobx-react-lite'
import './literal-err-page.less'

interface Props {
    blocks: LiteralErrBlock[]
}
const LiteralErrPage = observer(({ blocks }: Props) => {
    return <div className="literal-err-page">
        <h1>Literal Error</h1>
        {blocks.map(LiteralBlockView)}
    </div>
})

function LiteralBlockView(block: LiteralErrBlock, i: number) {
    return <div key={i} className="block">
        <div className="label">{block.label}</div>
        <div className="lines">
            {block.lines.map(LiteralLineView)}
        </div>
    </div>
}
function LiteralLineView(line: LiteralErrLine) {
    const line_cls = classNames('line', line.has_err && 'has-err')
    return <div key={line.n} className={line_cls}>
        <div className="line-n">{line.n}</div>
        <div className="line-main">
            <div className="line-text">{line.text}</div>
            {line.hint && <div className="line-hint">{line.hint}</div>}
        </div>
    </div>
}

export default LiteralErrPage
