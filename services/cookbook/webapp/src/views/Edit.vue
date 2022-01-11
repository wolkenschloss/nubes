<template>
  <v-dialog role="dialog" scrollable max-width="560px"
            v-model="dialog"
            :fullscreen="$vuetify.breakpoint.mobile">
    <v-card class="fab-container" height="560px" v-if="value">
      <v-toolbar>
        <v-btn icon @click="close">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>{{ title }}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn plain @click="save" v-bind:disabled="!valid">Save</v-btn>
        </v-toolbar-items>
        <template v-slot:extension>
          <v-tabs v-model="tab" fixed-tabs>
            <v-tabs-slider></v-tabs-slider>
            <v-tab key="0">Recipe</v-tab>
            <v-tab key="1">Ingredients</v-tab>
            <v-tab key="2">Preparation</v-tab>
          </v-tabs>
          <v-fab-transition>
              <v-btn :disabled="isLastIngredientEmpty" absolute fab small bottom left elevation="4"
                     accesskey="a"
                     color="primary"
                     v-if="tab === 1"
                     @click="addIngredient">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </v-fab-transition>
        </template>
      </v-toolbar>
      <v-card-text class="mt-6 fullscreen">
        <v-form v-model="valid" ref="form">
          <v-tabs-items v-model="tab">
            <v-tab-item key="0" >
              <v-text-field label="Title"
                            v-model="value.title"
                            :rules="titleRules"
                            required
                            autofocus>
              </v-text-field>
            </v-tab-item>
            <v-tab-item key="1" v-on:keyup.stop.insert="addIngredient">
              <servings autofocus v-model="value.servings" hint="Number of servings the recipe is designed for." class="mb-6"/>
              <v-expansion-panels class="pa-1" v-model="ingredientPanel">
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
                    <v-card-actions>
                      <v-spacer></v-spacer>
                      <v-btn text color="secondary" @click="removeIngredient(index)">Remove Item</v-btn>
                    </v-card-actions>
                  </v-expansion-panel-content>
                </v-expansion-panel>
              </v-expansion-panels>
            </v-tab-item>
            <v-tab-item key="2" >
              <v-textarea v-model="value.preparation"
                          prepend-icon="mdi-pencil"
                          counter
                          auto-grow
                          autofocus/>
            </v-tab-item>
          </v-tabs-items>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang=js>

import EditIngredient from "@/components/EditIngredient";
import Servings from "@/components/Servings";

export default {
  name: "Edit",
  props: ['title', 'value'],
  components: {Servings, EditIngredient},

  mounted() {
    // Die fab-transition wird nur aktiviert, wenn der state von false auf true gesetzt wird,
    // nachdem die Komponente erzeugt wurde.
    this.showFab = true
  },
  watch: {
    dialog(value) {
      if (value) {
        this.tab = null
        this.ingredientPanel = null
      } else {
        this.$refs.form.resetValidation()
      }
    }
  },
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
    },
    dialog: {
      get() {
        return this.value !== null
      },
      set(value) {
        if (!value) {
          this.$emit('cancel')
        }
        // ???
      }
    }
  },
  data() {
    return {
      showFab: false,
      ingredientPanel: null,
      tab: null,
      editable: this.$props.value || {},
      titleRules: [
        v => !!v || "Title is required"
      ],
      valid: false,
    }
  },
  methods: {
    close() {
      console.log(`edit.vue close dialog`)
      this.$emit('cancel', null)
    },
    save() {
      this.$emit('input', this.value)
    },
    addIngredient() {
      console.info("add ingredient")
      this.value.ingredients.push({quantity: null, unit: null, name: null})
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

.fullscreen {
  height: 100vh;
}

</style>