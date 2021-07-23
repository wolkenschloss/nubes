<template>
  <v-dialog v-model="value.active" :fullscreen="$vuetify.breakpoint.mobile" scrollable max-width="560px" max-height="560px">
    <v-card>
      <v-toolbar dark color="primary" class="flex-grow-0">
        <v-btn icon @click="cancel">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>Edit Ingredient</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items @click="save">
          <v-btn dark text>Save</v-btn>
        </v-toolbar-items>
      </v-toolbar>
        <v-card-title>Edit Ingredient</v-card-title>

      <v-card-text>
        <v-row>
          <v-col>
        <v-text-field label="Quantity" v-model="value.ingredient.quantity"></v-text-field>
          </v-col>
          <v-col>
        <v-select :items="units" label="Unit" v-model="value.ingredient.unit">
          <template v-slot:selection="{item}">
            <span>{{item.value}}</span>
          </template>
        </v-select>
          </v-col>
        </v-row>
        <v-text-field label="Ingredient" persistent-hint v-model="value.ingredient.name">
        </v-text-field>
      </v-card-text>
    </v-card>
  </v-dialog>
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