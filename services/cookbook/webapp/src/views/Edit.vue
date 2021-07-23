<template>
  <v-dialog scrollable max-width="560px" v-model="dialog" :fullscreen="$vuetify.breakpoint.mobile">
    <template v-slot:activator="{on, attrs}">
      <v-btn color="primary" elevation="2" fab bottom left fixed v-on="on">
        <v-icon>{{ fabIcon }}</v-icon>
      </v-btn>
    </template>

    <v-card style="position: relative">
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
          <v-btn dark absolute fab small bottom left color="green" elevation="2" v-if="tab === 1">
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </template>

      </v-toolbar>
      <v-card-title>Edit Recipe</v-card-title>
      <v-card-text style="height: 560px">
        <v-tabs-items v-model="tab">
          <v-tab-item key="0">
            <v-text-field label="Title" v-model="value.title"></v-text-field>
          </v-tab-item>
          <v-tab-item key="1">
            <v-expansion-panels class="pa-2">
              <v-expansion-panel v-for="(ingredient, index) in value.ingredients" :key="index">
                <v-expansion-panel-header>
                  <template v-slot:default="{open}">
                    <v-row no-gutters>
                      <v-col cols="12">
                        {{ingredient.quantity}} {{ingredient.unit}} {{ingredient.name}}
                      </v-col>
                    </v-row>
                  </template>
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <edit-ingredient v-model="value.ingredients[index]"></edit-ingredient>
                </v-expansion-panel-content>
              </v-expansion-panel>
              <v-expansion-panel>
                <v-expansion-panel-header>Neu</v-expansion-panel-header>
              </v-expansion-panel>
            </v-expansion-panels>

          </v-tab-item>
          <v-tab-item key="2">
            <v-textarea label="Preparation" v-model="value.preparation" prepend-icon="mdi-pencil"></v-textarea>
<!--            <editor v-model="value"></editor>-->
          </v-tab-item>
        </v-tabs-items>
      </v-card-text>

      <v-card-actions>
        <v-btn text>Close</v-btn>
        {{JSON.stringify(tab)}}
      </v-card-actions>


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
  data() {
    return {
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
  }
}

</script>

<style scoped>
/*.v-window-item {*/
/*  min-height: 100vh;*/
/*}*/
</style>