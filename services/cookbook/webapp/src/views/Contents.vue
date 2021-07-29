<template>
  <v-container>
    <v-data-table
        hide-default-header
        :items-per-page="$vuetify.breakpoint.mobile ? 5 : 10"
        :loading="loading"
        :items="recipes"
        :headers="headers"
        :options.sync="options"
        :server-items-length="total"
        item-key="recipeId"
        @click:row="onItemClick"
    >
      <template v-slot:footer.prepend>
      <edit fab-icon="mdi-plus" title="New Recipe" v-on:change="created" v-bind:value="recipe" v-on:cancel="cancel">
        <template v-slot:activator="{on, attrs}">
          <v-btn text color="secondary" v-on="on" v-bind="attrs">New Recipe</v-btn>
        </template>
      </edit>
      </template>
    </v-data-table>
  </v-container>
</template>

<script>

import axios from "axios";
import Edit from "@/views/Edit";

export default {
  name: 'Contents',
  components: { Edit },
  watch: {
    options: {
      handler() {
        this.loadRecipes()
      },
      deep: true
    }
  },
  data() {
    return {
      options: {},
      total: null,
      recipes: [],
      loading: true,
      recipe: {ingredients: []},
      headers: [
        {text: "Recipe", value: 'title'},
      ]
    }
  },
  async mounted() {
    await this.loadRecipes()
  },
  methods: {
    onItemClick(item) {
      this.$router.push({name: 'details', params: {id: item.recipeId}})
    },
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

      const {sortBy, sortDesc, page, itemsPerPage} = this.options
      const from = (page - 1)  * itemsPerPage;
      const to = from + itemsPerPage - 1;
      const uri = `/recipe?from=${from}&to=${to}`

      try {
        const response = await axios.get(uri)
        this.recipes = response.data.content
        this.total = response.data.total
        this.loading = false
      } catch (error) {
        this.loading = false
        alert(error);
      }
    }
  }
}
</script>
