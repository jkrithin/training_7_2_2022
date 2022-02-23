<template>
<v-app>
  <template v-if="!$route.path.includes('login')">

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
  <div style="height:50px;width:50px;"></div>
    <div style="width:500px;">
      <p class="mx-4">Number of Contacts : {{ newid-1 }}</p>
      <v-text-field
          v-model="fullName"
          label="Full Name"
          outlined
          clearable
          class="mx-4"
      ></v-text-field>
      <v-text-field
          v-model="phoneNumber"
          label="Phone Number"
          outlined
          clearable
          class="mx-4"

      ></v-text-field>
      <v-btn
          class="mx-4"
          fab
          dark
          small
          color="primary"
          v-on:click="addContact"
      >
        <v-icon dark>
          mdi-plus
        </v-icon>
      </v-btn>
      <v-btn class="mx-0"
             fab
             dark
             small
             color="primary"
             v-on:click="searchContact"
      >
        <v-icon dark>
          mdi-magnify
        </v-icon>
      </v-btn>
    </div>
  <div style="height:50px;width:5px;"></div>
    <v-card
        max-width="800"
        class="mx-4"
    >
      <v-toolbar
          color="indigo"
          dark
      >
        <!--v-app-bar-nav-icon></v-app-bar-nav-icon-->
        <v-toolbar-title>Contacts</v-toolbar-title>
        <v-spacer></v-spacer>
        <!--v-btn icon>
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn-->
        <div>
        <v-select
            v-model="pageSize"
            :items="items"
            class="pt-8"
            label="Contacts per Page"
            @input="updatePage"
        ></v-select>
        </div>
      </v-toolbar>
      <v-list>
        <v-list-item
            v-for="row in rows"
            :key="row.id"
        >
          <v-list-item-icon>
            <v-icon
                v-if="row.icon"
                color="pink"
            >
              mdi-star
            </v-icon>
          </v-list-item-icon>
          <v-list-item-avatar>
            <v-img :src="require('./assets/logo.svg')"></v-img>
          </v-list-item-avatar>
          <v-list-item-content>
            <!--v-list-item-title v-text="row.id" align="center"></v-list-item-title-->
            <v-list-item-title v-text="row.name"></v-list-item-title>
            <v-list-item-title v-text="row.phonenumber"></v-list-item-title>
          </v-list-item-content>
          <v-list-item-avatar>
            <v-btn
                class="mx-2"
                fab
                dark
                small
                color="primary"
                v-on:click="editContact(row.id)"
            >
              <v-icon dark>
                mdi-pencil
              </v-icon>
            </v-btn>
          </v-list-item-avatar>
          <v-list-item-avatar>
            <v-btn
                class="mx-2"
                fab
                dark
                small
                color="primary"
                v-on:click="removeContact(row.id)"
            >
              <v-icon dark>
                mdi-minus
              </v-icon>
            </v-btn>
          </v-list-item-avatar>
        </v-list-item>
      </v-list>
      <div class="text-center">
        <v-pagination
            v-model="currentPage"
            :length="totalPages"
            :total-visible="8"
            @input="updatePage"
        >
        </v-pagination>
      </div>
    </v-card>
  </template>
    <v-main>
        <keep-alive :include="['Login']">
          <router-view></router-view>
        </keep-alive>
    </v-main>
  </v-app>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      loading: false,
      rows: [],
      visibleRows:[],
      newid: 0,
      fullName:"",
      phoneNumber: "",
      currentPage: 1,
      pageSize:5,
      totalPages:1,
      items:[5,10,15],
      whenerror:false

    }
  },
  created() {
    this.getDataFromApi();
  },
  beforeMount() {
    //this.getDataFromApi();
    //this.totalPages = Math.floor(this.newid / this.pageSize);
  },
  mounted() {
    //this.totalPages = Math.floor(this.newid / this.pageSize);
  },
  beforeUpdate() {
    //this.totalPages = Math.floor(this.newid / this.pageSize);
    //console.log('before update '+this.totalPages);
  },
  methods: {
    async getDataFromApi() {

      this.loading = true
      try {
        const response = await axios.get('http://localhost:8080/phonebook/size');
        this.newid = response.data + 1
        console.log(this.newid)
      }catch (error){

        console.log(error)
        this.whenerror = true;
      }finally {
        this.loading = false
      }

      this.totalPages = Math.floor(this.newid / this.pageSize);
      this.updatePage();
    },

    addContact: function () {

      this.newid++;
      this.loading = true
      axios.post('http://localhost:8080/phonebook',{"id": this.newid ,"name": this.fullName,"phonenumber": this.phoneNumber})
          .then(response => {
            this.loading = false
            this.rows = response.data
          })
          .catch(error => {
            this.loading = false
            //console.log(error)
            this.whenerror = "Your Contact could not be added";
          })
      //this.getDataFromApi();
      this.updatePage();
    },

    removeContact: function (index) {
      //alert('removing: ' + index + ' !')
      //this.updateVisibleRows();

      this.loading = true
      axios.delete('http://localhost:8080/phonebook/'+index)
          .then(response => {
            this.loading = false
            this.rows = response.data
          })
          .catch(error => {
            this.loading = false
            console.log(error)
            //alert('Your Contact could not be added');
          })
      //this.getDataFromApi();
      this.updatePage();
    },

    searchContact: function () {
      console.log('NAME '+this.fullName);
      console.log('PHONE '+this.phoneNumber);

      if ((!this.fullName) && (!this.phoneNumber)){
        //alert('Please enter a value in Full Name or Phone Number ! ')
        //this.updatePage();
        this.getDataFromApi();

      }else{
        //this.currentPage=1;
        //this.totalPages = 1;
        //this.updatePage();
        //alert('values in Full Name or Phone Number '+this.fullName+' and '+this.phoneNumber)
        if ((this.fullName==="")||(this.fullName==null)){
          this.loading = true
          this.rows=[]
          axios.get('http://localhost:8080/phonebook/sp/'+this.phoneNumber + '?page='+this.currentPage+'&limit='+this.pageSize)
              .then(response => {
                this.data = response.data;
                this.data.forEach((item) => {
                  console.log("found id: ", item.id)
                  console.log("found name: ", item.name)
                  console.log("found phonenumber: ", item.phonenumber)
                  this.rows.push(item)
                });
              })
        }
        else if ((this.phoneNumber==="")||(this.phoneNumber==null)) {

          this.loading = true
          this.rows = [];
          axios.get('http://localhost:8080/phonebook/sn/' + this.fullName + '?page='+this.currentPage+'&limit='+this.pageSize)
              .then(response => {
                this.data = response.data;
                this.data.forEach((item) => {
                  console.log("found id: ", item.id)
                  console.log("found name: ", item.name)
                  console.log("found phonenumber: ", item.phonenumber)
                  this.rows.push(item)

                });
              })
        }else{
          //snp/{name}/{phonenumber}
          this.loading = true
          this.rows = [];
          axios.get('http://localhost:8080/phonebook/snp/' + this.fullName +'/'+this.phoneNumber+ '?page='+this.currentPage+'&limit='+this.pageSize)
              .then(response => {
                this.data = response.data;
                this.data.forEach((item) => {
                  console.log("found id: ", item.id)
                  console.log("found name: ", item.name)
                  console.log("found phonenumber: ", item.phonenumber)
                  this.rows.push(item)
                });
              })
        }
      }

      axios.get('http://localhost:8080/phonebook/searchsize?name='+this.fullName+'&phonenumber='+this.phoneNumber)
          .then(response => {
            this.loading = false

            this.totalPages = Math.floor(response.data / this.pageSize) + 1 ;
            console.log(this.totalPages)
          })
          .catch(error => {
            this.loading = false
            console.log(error)
          })


    },
    editContact: function (index) {
      alert('editing: ' + index + ' !')
      //put request
      //at http://localhost:8080/phonebook/2/6947650188
      if ((this.fullName == null) && (this.phoneNumber == null)) {
        alert('Please enter a value in Full Name or Phone Number ! ')
      } else {

        this.loading = true
        axios.put('http://localhost:8080/phonebook/' + index + '/' + this.fullName + '/' + this.phoneNumber)
            .then(res => {
              const newItem = {
                id: res.data.id,
                name: res.data.name,
                phonenumber: res.data.phonenumber,
              };
              this.rows = [];
              this.rows.push(newItem);
            })


      }
      this.updatePage();
    },

    updatePage: function ()
    {

      this.whenerror = false;
      axios.get('http://localhost:8080/phonebook?page='+this.currentPage+'&limit='+this.pageSize)
          .then(response => {
            this.loading = false
            this.rows = response.data

          })
          .catch(error => {
            this.loading = false
            console.log(error)
            this.whenerror = true;
          })

    },

    updateVisibleRows: function ()
    {
      //console.log('ok '+this.rows)
      //this.totalPages = Math.floor(this.newid / this.pageSize);
      //console.log('current page is: ' + this.currentPage)
      //console.log('total pages are: ' + this.totalPages)
      //this.visibleRows = this.rows.slice(this.currentPage * this.pageSize, (this.currentPage * this.pageSize) + this.pageSize);

      // zero visible, go back a page
     // if (this.visibleRows.length === 0 && this.currentPage > 0) {
     //   this.updatePage(this.currentPage - 1);
     // }
    }


  }
}
</script>
