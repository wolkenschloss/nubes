<template>
  <v-container>
    <v-text-field v-model="search"
                  append-icon="mdi-magnify"
                  label="Search"
                  single-line
                  hide-details
                  clearable
    />
    <v-data-table
        class="mt-4"
        hide-default-header
        :items-per-page="$vuetify.breakpoint.mobile ? 5 : 10"
        :footer-props="{'items-per-page-options': [5, 10, 15, 20]}"
        :loading="loading"
        :items="recipes"
        :headers="headers"
        :options.sync="options"
        :server-items-length="total"
        item-key="recipeId"
        @click:row="onItemClick"
    >
      <!--suppress HtmlUnknownAttribute -->
      <template v-slot:footer.prepend>
        <edit fab-icon="mdi-plus" title="New Recipe" v-on:change="created" v-bind:value="recipe" v-on:cancel="cancel">
          <template v-slot:activator="{on, attrs}">
            <v-btn v-on="on" v-bind="attrs" color="secondary">New Recipe</v-btn>
          </template>
        </edit>
      </template>
    </v-data-table>
  </v-container>
</template>

<script>

import axios from "axios";
import Edit from "@/views/Edit";
import {debounce} from "lodash";

export default {
  name: 'Contents',
  components: {Edit},
  watch: {
    options: {
      handler() {
        this.loadRecipes(this.search)
      },
      deep: true
    },
    search: debounce(function (val) {
      this.loadRecipes(val)
    }, 500),
  },
  data() {
    return {
      search: null,
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
        this.$router.push({name: 'details', params: {id: item['recipeId']}})
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
        this.options.page = 1
      } catch (error) {
        alert(error)
      }
    },
    async loadRecipes(query) {

      this.loading = true;

      const {page, itemsPerPage} = this.options
      const from = (page - 1) * itemsPerPage;
      const to = from + itemsPerPage - 1;

      console.log(`load recipe from ${from} to ${to} search '${query || ''}'`)
      const uri = `/recipe?from=${from}&to=${to}&q=${query || ''}`

      try {
        const response = await axios.get(uri)
        this.recipes = response.data.content
        this.total = response.data.total
        console.log(`got ${this.total} items`)
        this.loading = false
      } catch (error) {
        this.loading = false
        alert(error);
      }
    }
  }
}
</script>
