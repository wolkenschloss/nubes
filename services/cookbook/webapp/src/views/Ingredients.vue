<template>
  <v-container>
    <v-data-iterator
        class="mt-4"
        :options.sync="pagination"
        :items-per-page="$vuetify.breakpoint.mobile ? 4 : 12"
        :footer-props="{'items-per-page-options': [4, 8, 12, 16]}"
        :loading="loading"
        :items="toc"
        :server-items-length="total"
    >
      <template v-slot:header>
        <v-toolbar elevation="0">
          <v-btn-toggle group dense mandatory v-model="view_mode" color="primary" class="mr-4">
            <v-btn>
              <v-icon>mdi-view-grid</v-icon>
            </v-btn>
            <v-btn>
              <v-icon>mdi-view-list</v-icon>
            </v-btn>
          </v-btn-toggle>
          <v-text-field v-model="search"
                        append-icon="mdi-magnify"
                        label="Search"
                        single-line
                        hide-details
                        clearable/>
        </v-toolbar>
      </template>
      <template v-slot:default="props">
        <v-row v-if="view_mode === 0">
          <v-col v-for="item in props.items"
                 :key="item.id"
                 cols="12"
                 sm="6"
                 md="4"
                 lg="3">
            <v-card>
              <v-card-title class="subheading font-weight-bold">
                {{ item.name }}
              </v-card-title>
            </v-card>
          </v-col>
        </v-row>
        <v-list v-else>
          <v-list-item v-for="item in props.items" :key="item.id">
            <v-list-item-content>
              <v-list-item-title>{{item.name}}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </template>
    </v-data-iterator>
  </v-container>
</template>

<script>
import { debounce } from "lodash";
import {mapActions, mapGetters} from 'vuex'

export default {
  name: "Ingredients",
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
  data() {
    return {
      loading: false,
      view_mode: 0
    }
  },

  computed: {
    ...mapGetters('ingredients', ['toc', 'total']),
    pagination: {
      get() {
        return this.$store.getters['ingredients/pagination']
      },
      set(value) {
        this.$store.commit('ingredients/setPagination', value)
      }
    },
    search: {
      get() {
        return this.$store.getters['ingredients/filter']
      },
      set: debounce(async function(value) {
        await this.$store.commit('ingredients/setFilter', value)
      }, 500)
    }
  },
  methods: {
    ...mapActions('ingredients', ["queryIngredients"]),
    async load() {
      this.loading = true
      await this.queryIngredients()
      this.loading = false
    }
  }
}
</script>

<style scoped>

</style>