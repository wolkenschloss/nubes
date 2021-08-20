<template>
<v-container>
    <v-card class="ma-4 pa-4" flat>
      <v-card-title v-text="recipe.title"></v-card-title>
      <preparation :text="recipe.preparation"></preparation>
      <v-list v-if="recipe.ingredients.length > 0">
        <v-list-group>
          <template v-slot:activator>
            <v-list-item-icon>
              <v-icon>mdi-food</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Ingredients</v-list-item-title>
          </template>
          <v-list-item v-for="(ingredient, index) in recipe.ingredients" :key="index">
            <v-list-item-content>
              <v-list-item-title>
                {{ingredient.quantity}} {{ingredient.unit}} {{ingredient.name}}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list-group>
      </v-list>

      <v-card-actions>
        <v-btn text v-on:click="deleteRecipe(id)">
          <v-icon>mdi-delete</v-icon>
          Delete
        </v-btn>
      </v-card-actions>
      <edit fab-icon="mdi-pencil" title="Edit Recipe"
            v-on:change="saveRecipe"
            v-bind:value="copy"
            v-on:cancel="cancelEdit"/>
    </v-card>
</v-container>
</template>

<script>
import Edit from "@/views/Edit";
import Preparation from "@/components/Preparation";
import { mapGetters, mapActions } from "vuex"

export default {
  name: "Recipe",
  components: {Preparation, Edit},
  props: {id: String},
  computed: {
    ...mapGetters(['recipe']),
    copy: {
      get() {
        return this.$store.getters.copy
      },
      set(value) {
        this.$store.commit('setCopy', value)
      }
    }
  },
  mounted() {
    console.log(`component details mounted. loading recipe ${this.$props.id}`)
    this.loadRecipe(this.$props.id)
  },
  methods: {
    ...mapActions(['loadRecipe', 'deleteRecipe', 'saveRecipe', 'cancelEdit']),
  }
}
</script>

<style scoped>

</style>