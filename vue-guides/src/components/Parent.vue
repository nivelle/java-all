<template>
  <div>
    <el-row>
      <el-col :span="24">
        <div class="grid-content bg-purple-dark"></div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="8">
        <div class="grid-content bg-purple"></div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content bg-purple-light"></div>
      </el-col>
      <el-col :span="8">
        <div class="grid-content bg-purple"></div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="6">
        <div class="grid-content bg-purple"></div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple-light"></div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple"></div>
      </el-col>
      <el-col :span="6">
        <div class="grid-content bg-purple-light"></div>
      </el-col>
    </el-row>

    <el-row>
      <el-col :style="{color:'#F56C6C'}">这里是父组件,可以接收当前引入的子组件传递过来的值：{{message}}</el-col>
    </el-row>

    <el-row>
      <el-col>
        <Child message="当前为父组件内容,父组件调用子组件,但是当前显示内容由父组件传递过去的，props来实现" @change='getVal'></Child>
      </el-col>
    </el-row>

    <el-button type="warning" @click="goChild">去子组件</el-button>

    <el-button type="danger" @click="goChildWithLink">去子组件链接带参数</el-button>

    <ContentSlot>
      <div>
        父组件向子组件分发的内容
      </div>
      <div slot="nameSlot1">
        父组件向子组件 具名分发1
      </div>
      <div slot="nameSlot2">
        父组件向子组件 具名分发2
      </div>
    </ContentSlot>
  </div>
</template>

<script>
import Child from '@/components/Child'
import ContentSlot from '@/components/ContentSlot'


export default {
  name: "Parent",
  components: {
    Child,
    ContentSlot
  },
  data() {
    return {
      message: '我来自父组件'
    }
  },
  methods: {
    getVal: function (val) {
      this.message = val;
    },
    goChild() {
      //路由用法,直接指定要去的组件地址,并且可以传递参数
      this.$router.push({name: 'Child', params: {id: "fuck"}})
    },

    goChildWithLink() {
      //路由用法,直接指定要去的组件地址,并且可以传递参数
      this.$router.push({name: 'Child', query: {id: "fuck and link"}})
    }
  }
}
</script>

<style scoped>
  .el-row {
    margin-bottom: 20px;
  }

  .el-col {
    border-radius: 4px;
  }

  .bg-purple-dark {
    background: #99a9bf;
  }

  .bg-purple {
    background: #d3dce6;
  }

  .bg-purple-light {
    background: #e5e9f2;
  }

  .grid-content {
    border-radius: 4px;
    min-height: 36px;
  }

  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
</style>
