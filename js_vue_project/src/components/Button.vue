



<template>
  <button @click="onClick()" :style="{ background: color}" class="btn">{{text}}</button>
</template>



<script>
export default {
  name: 'Button',
  props:{
    text: String,
    color: String,
  },
  methods:{
    onClick(){
      console.log(this.color + 'click')
      this.$emit('change-from',this.color)
    },
  },
}
</script>



<style scoped>
Button {
  width: 150px;
  height: 150px;
  border-radius: 30px;
}

</style>