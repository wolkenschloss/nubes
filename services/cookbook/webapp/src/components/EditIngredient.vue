<template>
  <v-row class="mt-1">
    <v-col cols="2">
      <v-text-field dense label="Quantity" v-model="value.quantity" :rules="quantityRules" ref="editQuantity" ></v-text-field>
    </v-col>
    <v-col cols="3">
      <v-select dense :items="units" label="Unit" v-model="value.unit" clearable @change="unitChanged">
        <template v-slot:selection="{item}">
          <span>{{ item.value }}</span>
        </template>
      </v-select>
    </v-col>
    <v-col>
      <v-combobox dense
                      :loading="loading"
                      label="Ingredient"
                      v-model="value.name"
                      :items="toc"
                      :search-input.sync="search"
                      no-filter
                      hide-no-data
                      hide-details
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
      units: [

        {header: "Volumes"},

        {value: "t", text: "teaspoon"},
        {value: "T", text: "tablespoon"},
        {value: "fl oz", text: "fluid ounce"},
        {value: "gill", text: "gill"},
        {value: "c", text: "cup"},
        {value: "pt", text: "pint"},
        {value: "qt", text: "quart"},
        {value: "gal", text: "gallon"},
        {value: "ml", text: "milliliter"},
        {value: "l", text: "litre"},
        {value: "dl", text: "deciliter"},
        {value: "cl", text: "centiliter"},

        {header: "Mass and Weight"},
        {value: "lb", text: "pound"},
        {value: "oz", text: "ounce"},
        {value: "mg", text: "milligram"},
        {value: "g", text: "gram"},
        {value: "kg", text: "kilogram"},

        {header: "Length"},
        {value: "mm", text: "millimeter"},
        {value: "cm", text: "centimeter"},
        {value: "m", text: "meter"},
        {value: "in", text: "inch"}
      ],
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
      async handler(val) {
        await this.$store.commit('ingredients/setFilter', val)
        await this.load()
      },
      deep: true
    }
  },
  mounted() {
    this.$store.commit("ingredients/setPagination", {page: 1, itemsPerPage: 5})
    // this.$store.commit("ingredients/setFilter", "")
  },
  computed: {
    ...mapGetters('ingredients', ['toc', 'total']),
    // search: {
    //   // get() {
    //   //   return this.$store.getters['ingredients/filter']
    //   // },
    //   set: debounce(async function(value) {
    //     await this.$store.commit('ingredients/setFilter', value)
    //   }, 500)
    // }
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