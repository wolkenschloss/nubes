<template>
<v-container>
    <v-card class="ma-4 pa-4" flat v-if="recipe">
      <v-card-title v-text="recipe.title"></v-card-title>
      <preparation :text="recipe.preparation"></preparation>

      <v-expansion-panels v-model="panel">
        <v-expansion-panel>
          <v-expansion-panel-header>
            Ingredients
          </v-expansion-panel-header>
          <v-expansion-panel-content>
           <servings v-bind:value="servings" hint="Number of servings that will be served" class="mb-6" @input="scale"></servings>
            <v-list v-if="ingredients.length > 0">
                <v-list-item v-for="(ingredient, index) in ingredients" :key="index">
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
        <v-btn text @click="destroy(id)">
          <v-icon>mdi-delete</v-icon>
          Delete
        </v-btn>
      </v-card-actions>
      <v-btn color="primary" elevation="2" fab bottom fixed right @click="onEdit" >
        <v-icon>mdi-pencil</v-icon>
      </v-btn>
      <edit title="Edit Recipe" v-bind:value="copy" @input="update" @cancel="cancel"/>
    </v-card>
</v-container>
</template>

<script>
import Edit from "@/views/Edit";
import Preparation from "@/components/Preparation";
import { mapGetters, mapActions, mapState } from "vuex"
import Servings from "@/components/Servings";

export default {
  name: "Recipe",
  components: {Servings, Preparation, Edit},
  props: {id: String},
  computed: {
    ...mapGetters('recipe', ['recipe', 'servings', 'ingredients']),
    ...mapState('recipe', ['copy']),
  },
  data() {
    return {
      panel: null,
    }
  },
  async created() {
    console.log(`component details mounted. loading recipe ${this.$props.id}`)
    await this.read(this.$props.id)
  },
  methods: {
    ...mapActions('recipe', ['read', 'destroy', 'update', 'cancel', 'scale', 'edit']),
    onEdit() {
      this.edit(this.recipe)
    }
  }
}
</script>

<style scoped>

</style>