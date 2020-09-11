<template>
  <div class="hello">
    <h1>{{ msg }}</h1>
    <h2>非前端程序员学习VUE</h2>
    <ul>
      <li>
        <router-link to="DataMethod">数据方法绑定</router-link>
      </li>
      <li>
        <router-link to="ComputedAttribute">计算属性和侦听器</router-link>
      </li>
      <li>
        <router-link to="ConditionalRendering">条件渲染</router-link>
      </li>
    </ul>
    <br>
    <ul>
      <li>
        <router-link to="ListRendering">列表渲染</router-link>
      </li>
      <li>
        <router-link to="Parent">父子组件关系</router-link>
      </li>
      <li>
        <router-link to="ContentSlot">内容分发</router-link>
      </li>
    </ul>
    <br>
    <ul>
      <li>
        <router-link to="ContentStore">状态管理</router-link>
      </li>
    </ul>

  </div>
</template>

<script>

export default {
  name: 'Index',
  data() {
    return {
      msg: '学习VUE的核心知识'
    }
  },
  created: function () {
    console.log("created 表示实例被创建，但是还未挂载组件")
  },
  mounted: function () {
    console.log("mounted 表示挂载组件完成")
  },
  updated: function () {
    console.log("updated 表示数据更新完成")
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  h1, h2 {
    font-weight: normal;
  }

  ul {
    list-style-type: none;
    padding: 0;
  }

  li {
    display: inline-block;
    margin: 0 10px;
  }

  a {
    color: #42b983;
  }
</style>
