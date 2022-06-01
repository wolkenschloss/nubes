<template>
  <v-container>
    <v-toolbar elevation="0" class="farbig">
      <v-toolbar-title v-if="searching">
        {{ this.total }} Recipes
      </v-toolbar-title>
      <v-btn color="primary" @click="edit({ingredients: [], servings: 1})"
             v-if="!searching"
             :icon="$vuetify.breakpoint.xsOnly">
        <v-icon class="d-flex d-sm-none">mdi-file-plus</v-icon>
        <span class="d-none d-sm-flex">New Recipe</span>
      </v-btn>
      <edit title="New Recipe" v-bind:value="recipe" @input="create" @cancel="cancel"/>&nbsp;
      <import-dialog v-if="!searching"/>&nbsp;
      <v-spacer/>
      <ExpandingSearchField v-model="search" @resize="(expanded) => this.searching = expanded"></ExpandingSearchField>
    </v-toolbar>
    <v-data-table
        class="mt-4"
        hide-default-header
        :items-per-page="$vuetify.breakpoint.mobile ? 5 : 10"
        mobile-breakpoint="500"
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
import ExpandingSearchField from "@/components/ExpandingSearchField";

export default {
  name: 'Contents',
  components: {ExpandingSearchField, ImportDialog, Edit},
  watch: {
    pagination: {
      async handler() {
        await this.load()
      },
      deep: true
    },
    search: {
      async handler() {
        console.log("search changed")
        await this.load()
      },
      deep: true
    }
  },
  computed: {
    ...mapGetters('toc', ['toc', 'total']),

    recipe: {
      get() {
        return this.$store.getters['recipe/copy']
      },
      set(value) {
        console.log(`!!!! contents set copy ${JSON.stringify(value)}`)
        if (value) {
          this.create(value)
        }
        this.edit(null)
        // this.$store.commit('recipe/setCopy', value)
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
      searching: false,
      headers: [
        {text: "Recipe", value: 'title'},
      ]
    }
  },
  created() {
    this.reset()
  },
  mounted() {
    console.log("Mounted")
    console.log("Breakpoint: ", this.$vuetify.breakpoint.name)
  },

  methods: {
    ...mapActions('toc', ["updateQuery", "queryRecipes"]),
    ...mapActions('recipe', ["reset", 'edit', 'create', 'cancel']),

    async load() {
      this.loading = true
      await this.queryRecipes()
      this.loading = false
    },
    onItemClick(item) {
      this.$router.push({name: 'details', params: {id: item['recipeId']}})
    },
  }
}
</script>

<!--<style lang="css" >-->

<!--@media only screen and (max-width: 600px) {-->
<!--  .v-data-footer {-->
<!--    justify-content: center;-->
<!--    padding-bottom: 8px;-->
<!--  }-->

<!--  .v-data-footer .v-data-footer__select {-->
<!--    margin: 0 auto;-->
<!--  }-->

<!--  .v-data-footer .v-data-footer__pagination {-->
<!--    width: 100%;-->
<!--    margin: 0;-->
<!--  }-->

<!--  .v-application&#45;&#45;is-ltr .v-data-footer__select .v-select {-->
<!--    margin: 5px 0 5px 13px;-->
<!--  }-->

<!--  .v-application&#45;&#45;is-rtl .v-data-footer__select .v-select {-->
<!--    margin: 5px 13px 5px 0;-->
<!--  }-->
<!--}-->
<!--</style>-->

