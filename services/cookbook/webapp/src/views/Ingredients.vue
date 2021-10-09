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
          <v-text-field v-model="search"
                        append-icon="mdi-magnify"
                        label="Search"
                        single-line
                        hide-details
                        clearable/>
        </v-toolbar>
      </template>
      <template v-slot:default="props">
        <v-row>
          <v-col v-for="(item, index) in props.items"
                 :key="item.id"
                 cols="12"
                 sm="6"
                 md="4"
                 lg="3">
            <v-card>
              <v-card-title class="subheading font-weight-bold">
                {{index}} {{item.id}} {{ item.name }}
              </v-card-title>
            </v-card>
          </v-col>
        </v-row>
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
    }
  },
  computed: {
    ...mapGetters('ingredients', ['toc', 'total']),
    numberOfPages() {
      return Math.ceil(this.items.length / this.itemsPerPage)
    },
    filteredKeys() {
      return this.keys.filter(key => key != 'Name')
    },
    pagination: {
      get() {
        return this.$store.getters['ingredients/pagination']
      },
      set(value) {
        this.$store.commit('ingredients/setIpagination', value)
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
    },
    nextPage() {
      if (this.page + 1 <= this.numberOfPages) this.page += 1
    },
    formerPage() {
      if (this.page - 1 >= 1) this.page -= 1
    },
    updateItemsPerPage(number) {
      this.itemsPerPage = number
    }
  }
}
</script>

<style scoped>

</style>