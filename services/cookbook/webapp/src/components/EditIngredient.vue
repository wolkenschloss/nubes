<template>
  <v-row class="mt-1">
    <v-col cols="2">
      <v-text-field dense label="Quantity" v-model="value.quantity"></v-text-field>
    </v-col>
    <v-col cols="2">
      <v-select dense :items="units" label="Unit" v-model="value.unit">
        <template v-slot:selection="{item}">
          <span>{{ item.value }}</span>
        </template>
      </v-select>
    </v-col>
    <v-col>
      <v-text-field dense label="Ingredient" persistent-hint v-model="value.name">
      </v-text-field>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "EditIngredient",
  props: ['value'],
  data() {
    return {
      // https://en.wikibooks.org/wiki/Cookbook:Units_of_measurement
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
      ]
    }
  },
  methods: {
    cancel() {
      console.log("EditIngredients cancel")
      this.value.active = false
      //this.$emit("input", this.value)
    },
    save() {
      console.log("EditIngredients save")
      console.log(JSON.stringify(this.value))
      this.value.active = false
      this.$emit("input", this.value)
    }
  }
}
</script>

<style scoped>

</style>