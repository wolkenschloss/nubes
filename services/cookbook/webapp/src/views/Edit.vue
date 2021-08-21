<template>
  <v-dialog scrollable max-width="560px" v-model="dialog" :fullscreen="$vuetify.breakpoint.mobile" @click:outside="closeDialog">
    <template v-slot:activator="{on, attrs}">
      <slot v-bind="{on, attrs}" name="activator">
        <v-fab-transition  appear>
        <v-btn color="primary" elevation="2" fab bottom fixed right v-on="on" v-bind="attrs" >
          <v-icon>{{ fabIcon }}</v-icon>
        </v-btn>
        </v-fab-transition>
      </slot>
    </template>
    <v-card class="fab-container" height="560px">
      <v-toolbar>
        <v-btn icon @click="closeDialog">
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
                   color="primary"
                   v-if="tab === 1"
                   @click="addIngredient">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </v-fab-transition>
        </template>

      </v-toolbar>
      <v-card-text class="mt-6 fullscreen">
        <v-form v-model="valid">
          <v-tabs-items v-model="tab">
            <v-tab-item key="0">
              <v-text-field label="Title" v-model="value.title" :rules="titleRules" required></v-text-field>
            </v-tab-item>
            <v-tab-item key="1">
              <v-text-field label="Servings"
                            v-model="value.servings"
                            type="number"
                            persistent-hint
                            :rules="servingRules"
                            required
                            hint="Number of servings the recipe is designed for.">
            </v-text-field>
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
            <v-tab-item key="2">
              <v-textarea v-model="value.preparation"
                          prepend-icon="mdi-pencil"
                          counter
                          auto-grow/>
            </v-tab-item>
          </v-tabs-items>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>

import EditIngredient from "@/components/EditIngredient";

export default {
  name: "Edit",
  props: ['fabIcon', 'title', 'value'],
  components: {EditIngredient},
  mounted() {
    // Die fab-transition wird nur aktiviert, wenn der state von false auf true gesetzt wird,
    // nachdem die Komponente erzeugt wurde.
    this.showFab = true
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
    }
  },
  data() {
    return {
      showFab: false,
      ingredientPanel: null,
      tab: null,
      dialog: false,
      editable: this.$props.value || {},
      titleRules: [
        v => !!v || "Title is required"
      ],
      servingRules: [
        value => (!!value && value > 0 && value < 101) || "The number of servings must be between 1 and 100"
      ],
      valid: false,
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