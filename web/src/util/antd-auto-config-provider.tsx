import React, { ReactNode, useEffect } from 'react'
import { ConfigProvider, theme, ThemeConfig } from 'antd'

function get_is_dark_mode() {
    return window.matchMedia('(prefers-color-scheme: dark)').matches
}

interface Props {
    children?: ReactNode
}

function AntdAutoConfigProvider({ children }: Props) {
    const [is_dark_mode, set_is_dark_mode] = React.useState(get_is_dark_mode())
    useEffect(() => {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
            set_is_dark_mode(get_is_dark_mode())
        })
    }, [])

    const antd_theme_config: ThemeConfig = {
        algorithm: is_dark_mode ? theme.darkAlgorithm : theme.defaultAlgorithm,
    }

    return (
        <ConfigProvider theme={antd_theme_config}>
            {children}
        </ConfigProvider>
    )
}
export default AntdAutoConfigProvider
