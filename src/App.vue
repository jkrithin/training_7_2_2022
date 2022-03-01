<template>
<v-app>

    <v-alert
        v-model="whenerror"
        shaped
        prominent
        type="error"
        class="mx-4"
        max-width="800"
        max-height="100"
    >
      An unexpected error happened. Please try again later !
    </v-alert>

    <v-main>
        <keep-alive :include="['Login']">
          <router-view></router-view>
        </keep-alive>
    </v-main>
  </v-app>
</template>


<script>
  import axios from 'axios'
  import Login from "@/components/Login";
  import {mapGetters} from "vuex";

  export default {
    data() {
      return {
        whenerror: false
      }
    }
  }
</script>
