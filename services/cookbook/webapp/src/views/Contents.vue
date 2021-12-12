<template>
  <v-container>
    <v-toolbar elevation="0">
      <v-btn color="secondary" v-shortcut="'n'" @click="edit({ingredients: [], servings: 1})">
        New Recipe
      </v-btn>
      <edit fab-icon="mdi-plus" title="New Recipe" @change="change" v-bind:value.sync="copy" @cancel="edit(null)">
      </edit>&nbsp;
      <import-dialog/>&nbsp;
      <v-text-field v-model="search"
                    append-icon="mdi-magnify"
                    label="Search"
                    single-line
                    hide-details
                    clearable/>
    </v-toolbar>
    <v-data-table
        class="mt-4"
        hide-default-header
        :items-per-page="$vuetify.breakpoint.mobile ? 5 : 10"
        :footer-props="{'items-per-page-options': [5, 10, 15, 20]}"
        :loading="loading"
        :items="toc"
        :headers="headers"
        :options.sync="pagination"
        :server-items-length="total"
        item-key="recipeId"
        @click:row="onItemClick"/>
  </v-container>
</template>

<script>

import Edit from "@/views/Edit";
import {debounce} from "lodash";
import {mapActions, mapGetters} from 'vuex'
import ImportDialog from "@/components/ImportDialog";

export default {
  name: 'Contents',
  components: {ImportDialog, Edit},
  directives: {
    shortcut: {
      bind(el, binding) {
        console.log(`binding shortcut ${binding.value}`)
      },
      unbind(el, binding) {
        console.log(`unbinding shortcut ${binding.value}`)
      }
    }
  },
  watch: {
    pagination: {
      async handler() {
        await this.load()
      },
      deep: true
    },
    search: {
      async handler() {
        await this.load()
      },
      deep: true
    }
  },
  computed: {
    ...mapGetters('toc', ['toc', 'total']),

    copy: {
      get() {
        return this.$store.getters['recipe/copy']
      },
      set(value) {
        this.$store.commit('recipe/setCopy', value)
      }
    },
    pagination: {
      get() {
        return this.$store.getters['toc/pagination']
      },
      set(value) {
        this.$store.commit('toc/setPagination', value)
      }
    },
    search: {
      get() {
        return this.$store.getters['toc/filter']
      },
      set: debounce(async function (value) {
        await this.$store.commit('toc/setFilter', value)
      }, 500)
    }
  },
  data() {
    return {
      loading: false,
      headers: [
        {text: "Recipe", value: 'title'},
      ]
    }
  },
  created() {
    this.newRecipe()
  },
  mounted() {
    console.log("Mounted")
  },

  methods: {
    ...mapActions('toc', ["updateQuery", "queryRecipes"]),
    ...mapActions('recipe', ["newRecipe", 'createRecipe', 'edit']),
    klicki() {
      console.log("Keyboard ALT gedrückt.")
    },
    async load() {
      this.loading = true
      await this.queryRecipes()
      this.loading = false
    },
    onItemClick(item) {
      this.$router.push({name: 'details', params: {id: item['recipeId']}})
    },
    async change(recipe) {
      console.log(`contents change: ${JSON.stringify(recipe)}`)
      this.edit(null)
      await this.createRecipe(recipe)
      this.newRecipe()
      this.queryRecipes()
    }
  }
}
</script>
