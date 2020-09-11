<template>
  <div>

    <el-row v-for="item in items" :key="index">
      <el-col :span="7">{{ item.message }}</el-col>
    </el-row>
    <!-- index是索引值,从0开始 -->
    <el-row v-for="(item, index) in items" :key="index">
      <el-col :span="7"> 索引:{{index}}; 对应值:{{ item.message }}</el-col>
    </el-row>
    <el-row v-for="value in person" :key="index">
      <el-col :span="7"> 人实例属性:{{value}}</el-col>
    </el-row>
    <el-row v-for="(value,name) in person" :key="index">
      <el-col :span="7">{{name}}:{{value}}</el-col>
    </el-row>

    <el-row v-for="(value,name,index) in person" :key="index">
      <el-col :span="7">{{index}}-{{name}}:{{value}}</el-col>
    </el-row>

    <!--<el-row v-for=" item in evenNumbers">-->
      <!--<el-col :span="7">{{item}}</el-col>-->
    <!--</el-row>-->

    <el-button type="primary" plain @click="addItem">添加数组元素</el-button>

    <el-button type="primary" icon="el-icon-edit" @click="modifyItem">修改数组元素</el-button>

    <el-button type="danger" @click="removeItem">移除数组元素</el-button>

    <el-button type="warning" round @click="modifyObjectItem">修改对象元素</el-button>

    <el-button type="warning" round @click="getMethod">axios 测试</el-button>
  </div>
</template>
<script>

export default {
  name: "ListRendering",
  data() {
    return {
      items: [
        {id: 1, message: '我是1'},
        {id: 2, message: '我是2'},
        {id: 3, message: '我是3'},
        {id: 4, message: '我是4'},
        {id: 5, message: '我是5'},
        {id: 6, message: '我是6'},

      ],
      person: {
        name: "nivelle",
        age: 10,
        sex: "lady",
      }
    }
  },
  computed: {
    evenNumbers: function () {
      //数组过滤
      return this.items.filter(function (item) {
        return item.id > 1
      })
    }
  },
  methods: {
    addItem() {
      this.items.push({id: 3, message: '我是3'})
    },
    removeItem() {
      this.items.pop();
    },
    modifyItem() {
      this.items[1] = {id: 2, message: '我是直接操作2变异的2'}
      //vue的类似java的类方法，直接操作数组是不会触发响应式的数据修改，同时修改数组长度使用 vm.items.splice(newLength)
      this.$set(this.items, 1, {id: 2, message: '我是变异2'})
    },
    modifyObjectItem() {
      this.$set(this.person, 'age', 20);
    },
    getMethod(){
      this.$api.doGet('test/extends').then(e => {
        this.$message.success(e)
      })
    }
  }
}
</script>

<style scoped>

</style>
