<template>
  <v-container>
    <v-toolbar elevation="0">
      <edit fab-icon="mdi-plus" title="New Recipe" v-on:change="change" v-bind:value="copy" v-on:cancel="newRecipe">
        <template v-slot:activator="{on, attrs}">
          <v-btn v-on="on" v-bind="attrs" color="secondary">New Recipe</v-btn>
        </template>
      </edit>&nbsp;
      <import-dialog/>
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
    ...mapGetters(['toc', 'total']),
    copy: {
      get() {
        return this.$store.getters.copy
      },
      set(value) {
        this.$store.commit('setCopy', value)
      }
    },
    pagination: {
      get() {
        return this.$store.getters.pagination
      },
      set(value) {
        this.$store.commit('setPagination', value)
      }
    },
    search: {
      get() {
        return this.$store.getters.filter
      },
      set: debounce(async function (value) {
        await this.$store.commit('setFilter', value)
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
  methods: {
    ...mapActions(["updateQuery", "queryRecipes", "newRecipe", 'createRecipe']),
    async load() {
      this.loading = true
      await this.queryRecipes()
      this.loading = false
    },
    onItemClick(item) {
      this.$router.push({name: 'details', params: {id: item['recipeId']}})
    },
    change() {
      this.createRecipe()
      this.newRecipe()
      this.queryRecipes()
    }
  }
}
</script>
