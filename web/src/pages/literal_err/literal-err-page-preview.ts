import LiteralErrPage from './literal-err-page.tsx'

const page_msg_str = '{"type":"literal_err","data":[{"file":"cn-icbc.bean","lines":[{"n":186,"text":"2024-01-01 balance Assets:CN:ICBC                          4.55 CNY","has_err":false,"hint":""},{"n":187,"text":"2024-01-01 balance Assets:CN:ICBC:Fund                 33610.44 CNY","has_err":false,"hint":""},{"n":188,"text":"xxx","has_err":true,"hint":"本行格式不对"},{"n":189,"text":"","has_err":false,"hint":""}]},{"file":"cn-icbc.bean","lines":[{"n":194,"text":"2024-01-01 custom Assets:CN:ICBC:Fund Assets:CN:ICBC     897.15 CNY","has_err":false,"hint":""},{"n":195,"text":"","has_err":false,"hint":""},{"n":196,"text":"xxx","has_err":true,"hint":"本行格式不对"},{"n":197,"text":"","has_err":false,"hint":""}]}]}'
const page_msg = JSON.parse(page_msg_str)

function LiteralErrPagePreview() {
    return LiteralErrPage(page_msg.data)
}
export default LiteralErrPagePreview
