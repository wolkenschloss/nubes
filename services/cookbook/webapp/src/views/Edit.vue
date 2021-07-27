<template>
  <v-dialog scrollable max-width="560px" v-model="dialog" :fullscreen="$vuetify.breakpoint.mobile">
    <template v-slot:activator="{on, attrs}">
      <v-btn color="primary" elevation="2" fab bottom right fixed v-on="on">
        <v-icon>{{ fabIcon }}</v-icon>
      </v-btn>
    </template>

    <v-card class="fab-container">
      <v-toolbar dark color="primary" extended>
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn dark text @click="save">Save</v-btn>
        </v-toolbar-items>
        <template v-slot:extension>
          <v-tabs v-model="tab" fixed-tabs>
            <v-tabs-slider></v-tabs-slider>
            <v-tab key="0">Recipe</v-tab>
            <v-tab key="1">Ingredients</v-tab>
            <v-tab key="2">Preparation</v-tab>
          </v-tabs>
          <v-fab-transition>
            <v-btn :disabled="isLastIngredientEmpty" dark absolute fab small bottom left color="green" elevation="2"
                   v-if="tab === 1"
                   @click="addIngredient">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </v-fab-transition>
        </template>

      </v-toolbar>
      <v-card-title>Edit Recipe</v-card-title>
      <v-card-title></v-card-title>
      <v-card-text style="height: 560px" class="mt-6">
        <v-tabs-items v-model="tab">
          <v-tab-item key="0">
            <v-text-field label="Title" v-model="value.title"></v-text-field>
          </v-tab-item>
          <v-tab-item key="1">
            <v-expansion-panels class="pa-2" v-model="ingredientPanel">
              <v-expansion-panel v-for="(ingredient, index) in value.ingredients" :key="index">
                <v-expansion-panel-header>
                  <template v-slot:default="{open}">
                    <v-row no-gutters>
                      <v-col cols="12">
                        {{ ingredient.quantity }} {{ ingredient.unit }} {{ ingredient.name }}
                      </v-col>
                    </v-row>
                  </template>
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <edit-ingredient v-model="value.ingredients[index]"></edit-ingredient>
                  <v-btn text color="secondary" @click="removeIngredient(index)">Remove Item</v-btn>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>
          </v-tab-item>
          <v-tab-item key="2">
            <v-textarea label="Preparation" v-model="value.preparation" prepend-icon="mdi-pencil"></v-textarea>
          </v-tab-item>
        </v-tabs-items>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>

import Editor from "../components/Editor";
import EditIngredient from "@/components/EditIngredient";

export default {
  name: "Edit",
  props: ['fabIcon', 'title', 'value'],
  components: {EditIngredient, Editor},
  computed: {
    // Bestimmt, ob eine neue Zutat hinzugefügt werden kann.
    // Besser wäre zu prüfen, ob die Eingabe der letzten Zutat
    // gültig ist.
    isLastIngredientEmpty() {
      if (this.value.ingredients && this.value.ingredients.length > 0) {
        const last = this.value.ingredients[this.value.ingredients.length - 1];
        return Object.keys(last).length === 0
      } else {
        return false
      }
    }
  },
  data() {
    return {
      ingredientPanel: null,
      tab: null,
      dialog: false,
      editable: this.$props.value || {}
    }
  },
  methods: {
    closeDialog() {
      this.$emit('cancel')
      this.dialog = false
    },
    save() {
      this.$emit('change', this.value)
      this.dialog = false;
    },
    addIngredient() {
      this.value.ingredients.push({})
      this.ingredientPanel = this.value.ingredients.length - 1
    },
    removeIngredient(index) {
      this.ingredientPanel = null
      this.value.ingredients.splice(index, 1)
    }
  }
}

</script>

<style scoped>
.fab-container {
  position: relative;
}
</style>