// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';//引入elementUI
import 'element-ui/lib/theme-chalk/index.css';
import * as api from '@/util/http'
import Vuex from 'vuex'
import store from '@/store/index'

Vue.config.productionTip = false
//设置全局引用
Vue.use(ElementUI)
Vue.use(Vuex)


Vue.prototype.$api = api

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  //状态管理
  store,
  components: {App},
  template: '<App/>'
})
