<template>
  <div>
    <h1>{{ msg }}</h1>
    <h1>{{ obj }}</h1>
    <el-button type="primary" v-on:click="changeObj">click me</el-button>
    <el-row>
      <el-col :span="4">
        <div class="grid-content bg-purple">
          <el-button type="primary" v-on:click="changeContent">动态修改内容v-on动态绑定</el-button>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="grid-content bg-purple-light">
          <el-button type="warning" @click="changeContent">动态修改内容@click绑定</el-button>
        </div>
      </el-col>
      <el-col :span="4">
        <div class="grid-content bg-purple-light">
          <el-button type="warning" @click="showButton" v-bind:disabled="isShow">html特性不能使用双括号,需要使用v-bind绑定</el-button>
        </div>
      </el-col>

    </el-row>
    <el-row>
      <el-col :span="4">
        <p>大括号方式展示为文本: {{ rawHtml }}</p>
      </el-col>
      <el-col :span="4">
        <p>v-html方式展示为html标签语言: <span v-html="rawHtml"></span></p>
      </el-col>
      <el-col :span="4">
        <p v-if="seen" @click="showSeen">可以看见这段代码,点击后隐藏</p>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="4">
        <a :href="url">v-bind:href缩写为:href绑定</a>
      </el-col>

    </el-row>
  </div>
</template>

<script>

export default {
  name: 'DataMethod',
  data() {
    return {
      msg: '数据绑定测试',
      obj:{
        id:1,
        name:23,
        arr:[1,2,3]
      },
      arr:[1,2,3],
      rawHtml: '<span style="color:red">this is red</span>',
      isShow: false,
      seen: true,
      url: '/'
    }
  },
  methods: {
    changeContent() {
      this.msg = this.msg + "被修改了!"
    },
    showButton() {
      this.isShow = !this.isShow;
    },
    showSeen() {
      this.seen = false;
    },
    changeObj(){
      this.obj.asd=4;
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  h1, h2 {
    font-weight: normal;
    color: red;
  }
</style>
