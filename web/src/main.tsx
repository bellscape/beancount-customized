import React from 'react'
import ReactDOM from 'react-dom/client'
import HomePage from './pages/home-page.tsx'
import AntdAutoConfigProvider from './util/antd-auto-config-provider.tsx'

import 'antd/dist/reset.css'
import './main.css'
import './theme-light.less'
import './theme-dark.less'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import LiteralErrPagePreview from './pages/literal_err/literal-err-page-preview.ts'

const router = createBrowserRouter([
    { path: '/', element: <HomePage /> },
    { path: '/preview/literal-err', element: <LiteralErrPagePreview /> },
])

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AntdAutoConfigProvider>
            <RouterProvider router={router} />
        </AntdAutoConfigProvider>
    </React.StrictMode>,
)
