<template>
  <v-container>
    <v-data-iterator
        class="mt-4"
        :options.sync="pagination"
        :loading="loading"
        :items="toc"
        :server-items-length="total"
        :items-per-page.sync="itemsPerPage"
        hide-default-footer>
      <template v-slot:header>
        <v-toolbar dark color="primary darken-3" class="mb-3">
          <v-text-field v-model="search" clearable flat solo-inverted hide-details prepend-inner-icon="mdi-magnify"
                        label="Search"></v-text-field>
        </v-toolbar>
      </template>
      <template v-slot:default="props">
        <v-row>
          <v-col v-for="(item, index) in props.items"
                 :key="index"
                 cols="12"
                 sm="6"
                 md="4"
                 lg="3">
            <v-card>
              <v-card-title class="subheading font-weight-bold">
                {{ item.name }}
              </v-card-title>
              <v-divider></v-divider>
              <v-list dense>
                <v-list-item>
                  <v-list-item-content >
                    X
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-card>
          </v-col>
        </v-row>
      </template>
      <template v-slot:footer>
        <v-row class="mt-2" align="center" justify="center">
          <span class="grey--text">Items per page</span>
          <v-menu offset-y>
            <template v-slot:activator="{on, attrs}">
              <v-btn dark text color="primary" class="ml-2" v-bind="attrs" v-on="on">
                {{ itemsPerPage }}
                <v-icon>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-for="(number, index) in itemsPerPageArray" :key="index"
                           @click="updateItemsPerPage(number)">
                <v-list-item-title>{{ number }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
          <v-spacer></v-spacer>
          <span class="mr-4 grey--text">Page {{ page }} of {{ numberOfPages }}</span>
          <v-btn fab dark color="blue darken-3" class="mr-1" @click="formerPage">
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>
          <v-btn fab dark color="blue darken-3" class="ml-1" @click="nextPage">
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>
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
      itemsPerPageArray: [4, 8, 12],
      page: 1,
      itemsPerPage: 4,
      keys: ['Name', 'Calories', 'Fat', 'Carbs', 'Protein', 'Sodium', 'Calcium', 'Iron'],
      items: [],
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
        // await this.$store.commit('setIfilter', value)
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