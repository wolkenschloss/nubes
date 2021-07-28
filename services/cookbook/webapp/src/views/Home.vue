<template>
  <v-container>
    <list v-bind:recipes="this.$data.recipes" v-if="recipes && !loading"></list>
    <v-pagination v-model="page" v-bind:length="pages"></v-pagination>
    <v-skeleton-loader color="lighten-4" type="list-item" elevation="2" v-for="n in 10" :key="n" v-if="loading"/>
    <edit fab-icon="mdi-plus" title="New Recipe" v-on:change="created" v-bind:value="recipe" v-on:cancel="cancel"/>
    <p>{{ this.total }} / 5 = {{ this.pages }} page {{this.page}}</p>
    <p>count {{this.count}}</p>
  </v-container>
</template>

<script>

import axios from "axios";
import Edit from "@/views/Edit";

import List from "../components/List";

export default {
  name: 'Home',

  props: ['first', 'last'],
  components: {
    Edit,
    List,
  },
  watch: {
    page: {
      async handler(value) {
        console.log(`value change page = ${this.value} items per page ${this.count}`)

        const target = {
          query: {
            first: (value - 1) *  this.count,
            last: (value * this.count) - 1
          }
        }
        // Nicht schön!
        if (this.$route.query.first !== `${target.query.first}`) {
          await this.$router.push(target)
        }
      }
    },
    $route(to, from) {
      console.log(`route changed first = ${this.$props.first} last = ${this.$props.last}`)
      this.loadRecipes()
    }
  },
  computed: {
    pages() {
      return Math.ceil(this.total / 5);
    },
    count() {
      return (this.last - this.first) + 1
    }
  },
  data() {
    return {
      page: 1,
      total: 1,
      ipp: 5,
      recipes: {},
      loading: true,
      recipe: {ingredients: []}
    }
  },
  async mounted() {
    console.log(`mounting first = ${this.$props.first} last = ${this.$props.last}`)
    const p = Math.ceil(this.$props.first / this.count )
    console.log(`Selecting page ${p + 1}`)
    this.page = p + 1
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
      console.log("loading recipes")
      this.loading = true;
      let uri = "/recipe?from=" + this.$props.first + "&to=" + this.$props.last
      try {
        const response = await axios.get(uri)
        this.recipes = response.data.content
        this.total = response.data.total
        console.log(`Found ${this.total} recipes`)
        this.loading = false
      } catch (error) {
        this.loading = false
        alert(error);
      }
    },
  }
  }
</script>
