<template>
  <div>
    <h1>{{ msg }}</h1>
    <el-row>
      <el-col :span="10">
        反转字符串,计算:{{reverseMsg}}
      </el-col>
      <el-col :span="10">
        反转字符串,侦听:{{reverseMsg2}}
      </el-col>

      <el-col :span="2">
        <p :style="{color: 'green'}">内联样式绑定</p>
      </el-col>

      <el-col :span="2">
        <el-button type="primary" v-on:click="changeContent">修改内容</el-button>
      </el-col>

    </el-row>
  </div>
</template>

<script>
export default {
  name: "ConditionalRendering",
  //计算属性,复杂的双括号属性计算
  computed: {
    //需要计算属性的getter方法，依赖的值this.msg没有改变,会将计算后的值进行缓存,如果不希望缓存则改造为方法
    reverseMsg: function () {
      return this.msg.split('').reverse().join('')
    }
  },

  data() {
    return {
      msg: '对于需要复杂计算的双括号变量,需要使用计算属性',
      reverseMsg2: "hello"
    }
  }
  ,
  //侦听方式
  watch: {
    //msg的侦听方法
    msg: function () {
      this.reverseMsg2 = this.reverseMsg2.split('').reverse().join('')
    }
  },
  methods: {
    changeContent() {
      this.msg = this.msg.split('').reverse().join('');
    }
    ,
  }
}
</script>

<style scoped>
  h1, h2 {
    font-weight: normal;
    color: red;
  }
</style>
