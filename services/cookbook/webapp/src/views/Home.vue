<template>
    <v-container >
      <v-card min-height="30vh" class="pa-6">
        <list v-bind:recipes="this.$data.recipes" v-if="recipes && !loading"></list>
        <v-skeleton-loader  color="grey lighten-4" type="list-item" elevation="2" v-for="n in 4" v-if="loading"></v-skeleton-loader>
        <create v-on:created="created"/>
      </v-card>
    </v-container>
</template>

<script>

  import axios from "axios";

  import HelloWorld from '../components/HelloWorld'
  import List from "../components/List";
  import Create from "@/views/Create";

  export default {
    name: 'Home',

    components: {
      Create,
      List,
      HelloWorld,
    },

    data() {
      return {
        recipes: {},
        loading: true
      }
    },

    async mounted() {
      console.log("mounted")
      await this.loadRecipes()
    },

    async created() {
      console.log("created")
    },
    methods: {
      created(recipe) {
        this.recipes.unshift(recipe)
        this.loadRecipes();
      },
      async loadRecipes() {
        this.loading = true;
        let uri = "/recipe"
        try {
          const response = await axios.get(uri)
          this.recipes = response.data
          this.loading = false
        } catch (error) {
          alert(error);
        }
      }
    }
  }
</script>
