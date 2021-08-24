<template>
<v-container>
    <v-card class="ma-4 pa-4" flat>
      <v-card-title v-text="recipe.title"></v-card-title>
      <preparation :text="recipe.preparation"></preparation>

      <v-expansion-panels v-model="panel">
        <v-expansion-panel>
          <v-expansion-panel-header>
            Ingredients
          </v-expansion-panel-header>
          <v-expansion-panel-content>
           <servings v-model="servings" hint="Number of servings that will be served" class="mb-6" @input="servingsChanged"></servings>
            <v-list v-if="recipe.ingredients.length > 0">
                <v-list-item v-for="(ingredient, index) in recipe.ingredients" :key="index">
                  <v-list-item-content>
                    <v-list-item-title>
                      {{ingredient.quantity}} {{ingredient.unit}} {{ingredient.name}}
                    </v-list-item-title>
                  </v-list-item-content>
                </v-list-item>

            </v-list>

          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>

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
import Servings from "@/components/Servings";

export default {
  name: "Recipe",
  components: {Servings, Preparation, Edit},
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
    },
    servings: {
      get() {
        return this.$store.getters.servings
      },
      set(value) {
        this.$store.commit('setServings', value)
      }
    }
  },
  watch: {
    servings(newVal) {
      console.log(`watching serving(${newVal})`)
    }
  },
  data() {
    return {
      panel: null,
    }
  },
  mounted() {
    console.log(`component details mounted. loading recipe ${this.$props.id}`)
    this.loadRecipe(this.$props.id)
  },
  methods: {
    ...mapActions(['loadRecipe', 'deleteRecipe', 'saveRecipe', 'cancelEdit', 'scaleIngredients']),
    servingsChanged(value) {
      console.log(`servingsChanged(${value}`)
      this.scaleIngredients(value)
    }
  }
}
</script>

<style scoped>

</style>