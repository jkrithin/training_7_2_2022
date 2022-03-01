<template>
  <v-app id="inspire">
    <v-alert
        v-model="whenerror"
        shaped
        prominent
        type="error"
        class="mx-auto"
        max-width="800"
        max-height="100"
    >
      You could not be logged in. Please try again  !
    </v-alert>
    <v-main>
      <v-container fluid fill-height>
        <v-layout align-center justify-center>
          <v-flex xs12 sm8 md4>
            <v-card class="elevation-12">
              <v-toolbar dark color="primary">
                <v-toolbar-title>Login form</v-toolbar-title>
              </v-toolbar>
              <v-card-text>
                <v-form>
                  <v-text-field
                      v-model="username"
                      prepend-icon=mdi-account
                      name="login"
                      label="UserName / Email"
                      type="text"
                  ></v-text-field>
                  <v-text-field
                      v-model="password"
                      id="password"
                      prepend-icon=mdi-lock
                      name="password"
                      label="Password"
                      type="password"
                  ></v-text-field>
                </v-form>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn
                    color="primary"
                    v-on:click="attemptLogin()"
                >Login</v-btn>
              </v-card-actions>
            </v-card>
          </v-flex>
        </v-layout>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import axios from "axios";


export default {


  data() {
    return {
      user:{
        username: '',
        password: '',
        authenticated:false,
      },
      loading: false,
      name: 'Login',
      username: '',
      password: '',
      jwtToken:'',
      whenerror:false,
      loggedIn:false,
      context:'1',
    }


  },
  methods: {
    async attemptLogin() {
      this.loading = true

      console.log("Attempt to send login request");
      try {

        const response = await axios.post('http://localhost:8080/login',{"username": this.username,"password": this.password});
        this.jwtToken = response.data;
        this.loggedIn = true;
        sessionStorage.setItem("jwt", this.jwtToken);

      }catch (error){
        console.log(error)
      }finally {
        this.loading = false
      }

      if(this.loggedIn) {

        this.user = {"username": this.username,"password": this.password,authenticated: false};
        await this.$store.dispatch("login",[this.$data.context, this.user]);

      }else{

        // If not authenticated, add a path where to redirect after login.
        this.whenerror=true;

      }


    }
  },
};
</script>

<style></style>
