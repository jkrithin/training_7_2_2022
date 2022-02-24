<template>
  <v-app id="inspire">
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
      loading: false,
      name: 'Login',
      username: '',
      password: '',
      isAuthenticated:false
    }


  },
  methods: {
    async attemptLogin() {
      this.loading = true
      console.log("Attempt to send login request");
      try {

        const response = await axios.post('http://localhost:8080/login',{"username": this.username,"password": this.password});
        this.isAuthenticated = response.data;
        console.log('Am I authenticated? '+this.isAuthenticated)
      }catch (error){
        console.log(error)
      }finally {
        this.loading = false
      }

      if(!this.isAuthenticated) {
        // If not authenticated, add a path where to redirect after login.
        this.$router.push({ name: '', query: { redirect: '/login' } });
      }else{
        this.$router.push({ name: '/', query: { redirect: '/' } });
      }


    }
  },
};
</script>

<style></style>
