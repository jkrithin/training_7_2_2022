import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from "@/components/Login";
import App from "@/App";
import About from "@/views/AboutView.vue";
import Phonebook from "@/components/Phonebook";

Vue.use(VueRouter)


const router = new VueRouter({
  routes: [
    {
      path: '/login',
      component: Login,
    },
    {
      path: '/phonebook',
      component: Phonebook,
    },
    {
      path: '/about',
      component: About,
    }
  ],
});
export default router
