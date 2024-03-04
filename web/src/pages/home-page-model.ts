import { makeAutoObservable, observable } from 'mobx'

type PageType = 'empty' | 'err.literal' | 'err.balance' | 'home'

class HomePageModel {
    page_type: PageType = 'empty'
    page_data: any = {}
    socket: WebSocket

    constructor() {
        makeAutoObservable(this, { page_data: observable.ref })
        this.socket = new WebSocket(`ws://${location.host}/ws`)
        this.socket.onmessage = this.on_msg.bind(this)
        this.socket.onopen = () => { console.log('ws connected') }
        this.socket.onclose = () => { console.log('ws disconnected') }
        // this.socket.onerror = (error) => { console.error('error', error) }
        setInterval(() => {
            this.socket.send(JSON.stringify({ type: 'ping' }))
        }, 5000)
    }

    on_msg(event: MessageEvent<string>) {
        const msg = JSON.parse(event.data)
        if (msg.type === 'ping') return

        console.log('ws msg', event.data)
        this.page_type = msg.type
        this.page_data = msg.data
    }

}
export default HomePageModel
