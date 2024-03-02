import classNames from 'classnames'
import './literal-err-page.less'

type LiteralErrLine = {
    n: number
    text: string
    has_err: boolean
    hint: string
}
type LiteralErrBlock = {
    file: string
    lines: LiteralErrLine[]
}

function LiteralErrPage(blocks: LiteralErrBlock[]) {
    return <div className="literal-err-page">
        <h1>Literal Error</h1>
        {blocks.map(LiteralBlockView)}
    </div>
}
function LiteralBlockView(block: LiteralErrBlock, i: number) {
    return <div key={i} className="block">
        <div className="label">file: {block.file}</div>
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
            {line.hint && <div className="line-hint">{line.hint}</div>}
            <div className="line-text">{line.text}</div>
        </div>
    </div>
}

export default LiteralErrPage
