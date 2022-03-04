import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios';
import router from '@/router';
Vue.use(Vuex)


const STORAGE_KEY_USER = "random_user";


function hasTokenExpired(tokenExpiration) {
  return !tokenExpiration || (tokenExpiration < (Date.now() / 1000) - 60);
}
function jwtDecode(token) {
  var base64Url = token.split('.')[1];
  var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  var jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
    return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
  }).join(''));
  return JSON.parse(jsonPayload);
}

export default new Vuex.Store({
  state: {
    user:{
      username:'',
      role:'',
      country:'',
      token:'',
      tokenExpirationTime:'',
      authenticated:false
    }



  },
  getters: {
    loggedIn(state) {
      return state.user.authenticated && !hasTokenExpired(state.user.tokenExpirationTime);
    },


  },
  mutations: {
    persistUser(state, user) {
      console.log("Persist user was called !",user);
      state.user.authenticated = user != null && user.authenticated;
      if (user == null) {
        user = {};
      }
      state.user.username = user.username;
      state.user.role = user.role;
      state.user.country = user.country;
      state.user.token = user.token;
      state.user.tokenExpirationTime = user.tokenExpirationTime;
      localStorage.setItem("jwt",user.token);
      if (state.user.authenticated) {
        localStorage.setItem(STORAGE_KEY_USER, JSON.stringify(state.user));
      } else {
        localStorage.removeItem(STORAGE_KEY_USER);
      }
    },
  },
  actions: {
    async scheduleRefreshToken(context) {
      //schedule the 'refresh' method every minute
      const timerId = setInterval(() => context.dispatch('refreshToken'), 60000);
      //also trigger an immediate refresh
      await context.dispatch('refreshToken');
    },
    async refreshToken(context) {
      if (!context.getters.loggedIn) {
        return;
      }
    },
    async login(context, user) {

      try {
        console.log("Request sent: " , {"username":user[1].username,"password": user[1].password});
        const resp = await axios.post(`http://localhost:8080/login`, {"username":user[1].username,"password": user[1].password});
        await context.dispatch('handleLoginResponse', resp);
      } catch (e) {
        console.error("error logging in", e);
      }
    },
    async handleLoginResponse(context, resp) {


      const response = resp.data;
      var json = JSON.parse(resp.config.data);
      const username = json["username"];
      if (response){

        const token = resp.data.jwt;
        const jwtObj = jwtDecode(resp.data.jwt);
        const tokenExpirationTime = jwtObj.exp;

        if (hasTokenExpired(tokenExpirationTime)) {
          console.log("loginError token expired");
          return;
        }
        const role = jwtObj.groups[0];
        const country = jwtObj.country;

        context.commit('persistUser', {authenticated: true, username: username, role: role, country: country, token: token, tokenExpirationTime: tokenExpirationTime});
        if (role === "admin") {
          console.log("admin role ");
          router.push('/phonebook');
        }else{
          router.push('/about');
        }
      } else {
        console.log("loginError");
        router.push('/login');
      }
    },
  },
  modules: {
  }
})
