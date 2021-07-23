<template>
  <v-container>
    <list v-bind:recipes="this.$data.recipes" v-if="recipes && !loading"></list>
    <v-skeleton-loader color="lighten-4" type="list-item" elevation="2" v-for="n in 10" :key="n" v-if="loading"/>
    <edit fab-icon="mdi-plus" title="New Recipe" v-on:change="created" v-bind:value="recipe" v-on:cancel="cancel"/>
  </v-container>
</template>

<script>

import axios from "axios";
import Edit from "@/views/Edit";

import List from "../components/List";

export default {
  name: 'Home',

  components: {
    Edit,
    List,
  },

  data() {
    return {
      recipes: {},
      loading: true,
      recipe: {ingredients: []}
    }
  },
  async mounted() {
    await this.loadRecipes()
  },
  methods: {
    cancel() {
      this.recipe = {ingredients: []}
    },
    async created(recipe) {
      let uri = "/recipe"
      try {
        await axios.post(uri, recipe)
        await this.loadRecipes();
        this.recipe = {ingredients: []}
      } catch (error) {
        alert(error)
      }
    },
    async loadRecipes() {
      this.loading = true;
      let uri = "/recipe"
      try {
        const response = await axios.get(uri)
        this.recipes = response.data
        this.loading = false
      } catch (error) {
        this.loading = false
        alert(error);
        }
      }
    }
  }
</script>
