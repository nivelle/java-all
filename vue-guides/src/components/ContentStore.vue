<template>
  <div>
    <p>{{count}}</p>
    <el-button type="primary" plain @click="addCount">计数增加</el-button>
    <el-button type="primary" plain @click="descCount">计数减少</el-button>
  </div>
</template>

<script>
export default {
  name: "ContentStore",
  computed: {
    count(){
      return this.$store.state.count;
    }
  },
  methods:{
    addCount(){
      this.$store.commit('add');
    },
    descCount(){
      this.$store.commit('desc')
    }
  }
}
</script>

<style scoped>

</style>
