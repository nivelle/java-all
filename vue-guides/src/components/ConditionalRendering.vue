<template>
  <div>
    <h1 v-if="isShow">{{ msgTrue }}</h1>
    <h1 v-else="isShow">{{ msgFalse }}</h1>
    <el-row>
      <el-col :span="8">
        <el-button type="primary" @click="changeStatue">修改条件</el-button>
      </el-col>

      <el-col :span="8">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="用户名" v-if="type==='userName'">
            <el-input v-model="form.userName" placeholder="条件渲染v-fi" key="username-input"></el-input>
          </el-form-item>
          <el-form-item label="邮件地址" v-else>
            <el-input v-model="form.email" placeholder="条件渲染v-else" key="email-input"></el-input>
          </el-form-item>
        </el-form>
      </el-col>

      <!-- v-show 本质上是CSS 属性 display，不支持<template>也不支持v-else-->
      <el-col :span="8">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="用户名" v-show="isShow">
            <el-input v-model="form.userName" placeholder="v-show为真"></el-input>
          </el-form-item>
          <el-form-item label="邮件地址" v-show="!isShow">
            <el-input v-model="form.email" placeholder="v-show为假"></el-input>
          </el-form-item>
        </el-form>
      </el-col>

    </el-row>
  </div>
</template>

<script>
export default {
  name: "ConditionalRendering",
  data() {
    return {
      msgTrue: '条件渲染为真',
      msgFalse: '条件渲染为假',
      isShow: false,
      type: 'email',
      form: {
        userName: '',
        email: ''
      }
    }
  },
  methods: {
    changeStatue() {
      this.isShow = !this.isShow;

      if (this.type === 'userName') {
        this.type = 'email'
      } else {
        this.type = 'userName'
      }
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
