<template>
  <!--div :style="{'background-image': 'url(' + require('./assets/logo.png') + ')'}"-->
  <div :class="className">
    <Header title="Welcome to the Color changer">

    </Header>
    <Button @change-from="changeColor" color="Red" text="Toggle to Blue"></Button>
    <Button @change-from="changeColor" color="Green" text="Toggle to Red"></Button>
    <Button @change-from="changeColor" color="Blue" text="Toggle to Green"></Button>




  </div>


</template>

<script>

import Header from "./components/Header.vue";
import Button from "@/components/Button";
export default {
  name: 'App',
  components: {
    Header,
    Button,
  },
  data(){
    return{
      color: [],
      className : ""
    }
  },
  methods:{
    changeColor(color){

      if (color == "Red"){
        this.className = 'blue';
      }
      if (color == "Green"){
        this.className = 'red';
      }
      if (color == "Blue"){
        this.className = 'green';

      }


    }
  }
}

</script>

<style>

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #fe0000;
  margin-top: 10px;
  background-color: rgb(0, 0, 0);
  width: 850px;
  height: 850px;
  border-radius: 50px;

}
.red{
  background-color: red;
}
.blue{
  background-color: blue;
}
.green{
  background-color: green;
}

</style>
