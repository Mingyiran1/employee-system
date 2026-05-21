import { ref } from 'vue'

// 简单的事件总线，用于跨组件通信
const eventBus = {
  // 存储事件回调
  _events: {},

  // 监听事件
  on(event, callback) {
    if (!this._events[event]) {
      this._events[event] = []
    }
    this._events[event].push(callback)
  },

  // 触发事件
  emit(event, ...args) {
    if (this._events[event]) {
      this._events[event].forEach(callback => callback(...args))
    }
  },

  // 移除监听
  off(event, callback) {
    if (this._events[event]) {
      this._events[event] = this._events[event].filter(cb => cb !== callback)
    }
  }
}

export default eventBus
