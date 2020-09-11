//状态管理目的是解决跨组件共享数据的问题
import Vuex from 'vuex'
import Vue from 'vue'
Vue.use(Vuex)

export default new Vuex.Store({
  state:{
    count:0
  },
  mutations:{
    add:state=>state.count++,
    desc:state=>state.count--
  }
})
