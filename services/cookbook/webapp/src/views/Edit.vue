<template>
    <v-dialog scrollable max-width="560px" v-model="dialog" :fullscreen="$vuetify.breakpoint.mobile">
      <template v-slot:activator="{on, attrs}">
        <v-btn color="primary" elevation="2" fab bottom left fixed v-on="on">
          <v-icon>{{fabIcon}}</v-icon>
        </v-btn>
      </template>

    <v-card>
      <v-toolbar dark color="primary" >
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>{{title}}</v-toolbar-title>
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
        </template>
      </v-toolbar>
      <v-card-title>Edit Recipe</v-card-title>
      <v-card-text style="height: 560px">
        <v-tabs-items v-model="tab">
        <v-tab-item key="0">
          <v-text-field label="Title" v-model="value.title"></v-text-field>
        </v-tab-item>

        <v-tab-item key="2">
          <editor v-model="value"></editor>
        </v-tab-item>

        <v-tab-item key="1">

          <v-list>
            <v-list-item-group>
              <v-list-item v-for="(ingredient, index) in value.ingredients" :key="index" link @click="editIngredient(index)">
                <template v-slot:default="{active}">
                  <v-list-item-content>
                    <v-list-item-title>
                      {{ ingredient.name }}
                    </v-list-item-title>

                  </v-list-item-content>
                  <v-list-item-action>
                    <v-list-item-action-text>
                      {{ ingredient.quantity }} {{ ingredient.unit }}
                    </v-list-item-action-text>
                  </v-list-item-action>
                </template>
              </v-list-item>
              <v-divider></v-divider>
              <v-list-item>
                <v-list-item-title>
                  <v-btn @click="addIngredient">Add ingredient</v-btn>
                </v-list-item-title>
              </v-list-item>
            </v-list-item-group>
          </v-list>
          <v-fab-transition>
            <v-btn fab bottom left absolute><v-icon>mdi-plus</v-icon></v-btn>
          </v-fab-transition>
        </v-tab-item>
        </v-tabs-items>
      </v-card-text>


      <v-card-actions>
        <v-btn text>Close</v-btn>
      </v-card-actions>
    </v-card>
    </v-dialog>
</template>

<script>

import Editor from "../components/Editor";

export default {
  name: "Edit",
  props: ['fabIcon', 'title', 'value'],
  components: {Editor},
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