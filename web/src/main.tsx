import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'

import 'antd/dist/reset.css'
import './index.css'
import AntdAutoConfigProvider from './util/antd-auto-config-provider.tsx'

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AntdAutoConfigProvider>
            <App />
        </AntdAutoConfigProvider>
    </React.StrictMode>,
)
