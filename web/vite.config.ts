import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 5001,
        strictPort: true,
        open: true,
        proxy: {
            '/ws': {
                target: 'http://127.0.0.1:5002',
                ws: true,
            },
        },
    },
})
