<template>
  <div>
    <el-container>
      <el-header>上部头</el-header>
      <el-main>
        <slot name="nameSlot2">具名分发默认值2</slot>
      </el-main>
    </el-container>

    <el-container>
      <el-header>中间部头</el-header>
      <el-main>
        <slot>如果没有分发内容则展示这个默认值</slot>
      </el-main>
      <el-footer>中间尾部</el-footer>
    </el-container>

    <el-container>
      <el-aside width="200px">尾部头</el-aside>
      <el-main>
        <slot name="nameSlot1">具名分发默认值</slot>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: "ContentSlot"
}
</script>

<style scoped>

</style>
