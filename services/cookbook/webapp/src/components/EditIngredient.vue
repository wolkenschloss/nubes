<template>
  <v-row class="mt-1">
    <v-col cols="2">
      <v-text-field dense label="Quantity" v-model="value.quantity" :rules="quantityRules" ref="editQuantity" autofocus></v-text-field>
    </v-col>
    <v-col cols="4">
      <v-select dense :items="entries" label="Unit" v-model="value.unit" clearable @change="unitChanged" ref="editName" type="text">
        <template v-slot:selection="{item}">
          <span>{{ item.value }}</span>
        </template>
      </v-select>
    </v-col>
    <v-col>
      <v-combobox dense
                  :rules="nameRules"
                      :loading="loading"
                      label="Ingredient"
                      v-model="value.name"
                      :items="toc"
                      :search-input.sync="search"
                      no-filter
                      hide-no-data
                      item-text="name"
                      item-value="name"
                      :return-object="false"
      >
      </v-combobox>
    </v-col>
  </v-row>
</template>
<script>

// https://en.wikibooks.org/wiki/Cookbook:Units_of_measurement
import { debounce } from "lodash";
import {mapActions, mapGetters} from 'vuex'

export default {
  name: "EditIngredient",
  props: ['value'],
  data() {
    return {
      loading: false,
      search: null,
      items: [],
      nameRules: [
          v => !!v || 'Name is required'
      ],
      quantityRules: [
          v => (!this.$props.value.unit || !!v) || "Unit requires quantity",
          v =>  /^([1-9][0-9]*)*$/g.test(v || "") || "Value should be a number greater than 0"
      ]
    }
  },
  watch: {
    search: {
      handler: debounce(async function(value) {
        console.log(`debounced search: ${value}`)
        await this.$store.commit('ingredients/setFilter', value)
        await this.load()
      }, 500),
      deep: true
    }
  },
  mounted() {
    this.$store.commit("ingredients/setPagination", {page: 1, itemsPerPage: 5})
  },
  computed: {
    ...mapGetters('ingredients', ['toc', 'total']),
    ...mapGetters('units', ['entries'])
  },
  methods: {
    ...mapActions('ingredients', ["queryIngredients"]),

    async load() {
      this.loading = true
      await this.queryIngredients()
      this.loading = false
    },
    unitChanged() {
      this.$refs.editQuantity.validate(true);
    },
    cancel() {
      this.value.active = false
    },
    save() {
      this.value.active = false
      this.$emit("input", this.value)
    }
  }
}
</script>

<style scoped>

</style>