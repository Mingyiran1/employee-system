const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000,
    client: {
      overlay: false
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
        secure: false,
        pathRewrite: {
          '^/api': ''
        },
        onError: (err) => {
          console.log('代理错误:', err)
        },
        onProxyReq: (proxyReq, req) => {
          console.log('代理请求:', req.method, req.url, '->', proxyReq.path)
        }
      }
    }
  }
})
