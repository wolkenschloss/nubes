<template>
  <v-form>
    <v-text-field label="Rezeptname" v-model="model.title" prepend-icon="mdi-folder"></v-text-field>
    <v-textarea label="Rezeptzubereitung" v-model="model.preparation" prepend-icon="mdi-pencil"></v-textarea>
    <!--suppress JSUnusedLocalSymbols -->
    <v-text-field v-for="(ingredient, index) in model.ingredients" :key="index" v-model="model.ingredients[index]">
      <template v-slot:append>
        <v-btn text icon @click="deleteIngredient(index)">
          <v-icon>mdi-delete</v-icon>
        </v-btn>
      </template>
    </v-text-field>
    <v-text-field label="Ingredient" prepend-icon="mdi-food" v-on:keyup.enter="addIngredient" v-model="ingredient">
      <template v-slot:append>
        <v-btn text icon>
          <v-icon>mdi-plus</v-icon>
        </v-btn>
      </template>
    </v-text-field>
  </v-form>
</template>

<script>
export default {
  name: "Editor",
  props: ['value'],
  computed: {
    model: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    }
  },
  data() {
    return {
      ingredient: ""
    }
  },
  methods: {
    addIngredient() {
      console.log("add ingredient " + this.ingredient)

      let ingredients = this.$props.value.ingredients || []
      ingredients.push(this.ingredient)
      this.$props.value.ingredients = ingredients
      this.ingredient = ""
    },
    deleteIngredient(pos) {
      console.log(pos)
      this.$props.value.ingredients.splice(pos, 1)
    }
  }
}

</script>

<style scoped>

</style>