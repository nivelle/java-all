<template>
  <div>
    <el-form label-width="100px" class="demo-ruleForm">

      <el-form-item>
        <el-button type="primary" @click="sendMessage">这里是父组件引入的子组件，向父组件传值</el-button>
        <el-button type="info" @click="goBack">这里是子组件</el-button>
        <p>通过路由地址带到子组件的值:{{routerMsg}}</p>
      </el-form-item>
    </el-form>
    <el-row :gutter="12">

      <el-col :span="24">
        <el-card shadow="never" v-if="message">
          <p>这里是子组件位置：内容来自父组件 {{message}}</p>
        </el-card>
        <el-card shadow="never" v-else>
          <p>我是子组件在没有父组件传值时的默认值1</p>
        </el-card>
      </el-col>

    </el-row>
  </div>
</template>

<script>
export default {
  mounted() {
    let routerMsg = this.$route.params.id;
    this.routerMsg = routerMsg;
  },
  name: "Child",
  data() {
    return {
      childMsg: "我来自子组件",
      routerMsg:""
    };
  },
  //接收调用它的父组件的传递参数
  props: ["message"],
  methods: {
    sendMessage() {
      //父组件关注了子组件的change事件
      this.$emit("change", this.childMsg);
    },
    goBack() {
      //history用法，返回上n级
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>

</style>
