import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from "@/components/Login";
import Logout from "@/components/Logout";
import Store from '@/store';
import About from "@/views/AboutView.vue";
import Phonebook from "@/components/Phonebook";

Vue.use(VueRouter)


const router = new VueRouter({
  routes: [
    {path: '/', name: 'Phonebook', component: Phonebook},
    {path: '/login', name: 'Login', component: Login},
    {path: '/logout', name: 'Logout', component: Logout},
    {path: '/about', component: About},
    {path :'*', redirect: '/'},
  ]
});
  router.beforeEach((to, from, next) => {
    //if (Store.getters.loggedIn  && to.path !== '/logout') {
      //return next({path: '/'});
    //}

    if (to.path!=='/login'  && !Store.getters.loggedIn) {

      return next({ path: '/login' });
    }
    next();
});
export default router
