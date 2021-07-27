<template>
  <v-row class="mt-1">
    <v-col cols="2">
      <v-text-field dense label="Quantity" v-model="value.quantity" :rules="quantityRules" ref="inputQuantity"></v-text-field>
    </v-col>
    <v-col cols="3">
      <v-select dense :items="units" label="Unit" v-model="value.unit" clearable>
        <template v-slot:selection="{item}">
          <span>{{ item.value }}</span>
        </template>
      </v-select>
    </v-col>
    <v-col>
      <v-text-field dense label="Ingredient" persistent-hint v-model="value.name" :rules="nameRules" required>
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
      valid: false,
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
      ],
      nameRules: [
          v => !!v || 'Name is required'
      ],
      quantityRules: [
          v => (!this.$props.value.unit || !!v) || "Unit requires quantity",
          v => /^([1-9][0-9]*)*$/g.test(v) || "Value should be a number greater than 0"
      ]
    }
  },
  watch: {
    "value.unit": {
      deep: false,
      immediate: true,
      async handler()  {
        await this.$nextTick()
        this.$refs.inputQuantity.validate();
      }
    }
  },
  methods: {
    cancel() {
      console.log("EditIngredients cancel")
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