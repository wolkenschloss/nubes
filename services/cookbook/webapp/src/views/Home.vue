<template>
    <v-container >
      <v-card min-height="30vh">
        <list v-bind:recipes="this.$data.recipes" v-if="recipes"></list>
        <v-skeleton-loader v-else type="list"></v-skeleton-loader>

          <v-btn color="primary" elevation="2" fab bottom left fixed to="/create">
            <v-icon>mdi-plus</v-icon>
          </v-btn>

      </v-card>
    </v-container>
</template>

<script>

  import axios from "axios";

  import HelloWorld from '../components/HelloWorld'
  import List from "../components/List";

  export default {
    name: 'Home',

    components: {
      List,
      HelloWorld,
    },

    data() {
      return {
        recipes: {}
      }
    },

    mounted() {
      this.loadRecipes()
    },

    methods: {
      loadRecipes() {
        let uri = "/recipe"
        axios.get(uri)
        .then(response => {
          this.recipes = response.data;
        })
        .catch(error => {
          alert(error)
        })
      }
    }
  }
</script>
