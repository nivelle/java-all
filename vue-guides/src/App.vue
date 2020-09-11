<template>
  <div id="app">
    <link rel="shortcut icon" type="image/x-icon" href="./assets/logo.png" />
    <img src="./assets/logo.png">
    <!-- 通过路由进来的组件在这里展示-->
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 60px;
  }
</style>
