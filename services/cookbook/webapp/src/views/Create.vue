<template>
  <v-row justify="center">
    <v-dialog v-model="dialog" fullscreen hide-overlay scrollable transition="dialog-bottom-transition">
      <template v-slot:activator="{on, attrs}">
        <v-btn color="primary" elevation="2" fab bottom left fixed v-bind="attrs" v-on="on">
          <v-icon>mdi-plus</v-icon>
        </v-btn>
      </template>

    <v-card>
      <v-toolbar dark color="primary" class="flex-grow-0">
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>New Recipe</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn dark text @click="save">Save</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      <editor v-bind:recipe="this.$data.recipe"></editor>
    </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
import axios from "axios";
import Editor from "../components/Editor";

export default {
  name: "Create",
  components: {Editor},
  data() {
    return {
      recipe: {},
      dialog: false
    }
  },

  methods: {
    closeDialog() {
      console.log("Close Dialog")
      this.dialog = false
    },
    save() {
      console.log("Save Recipe")
      axios.post("/recipe", this.recipe)
      .then(() => {
        this.closeDialog()
      })
      .catch(error => alert(error))
    },

  }
}

</script>

<style scoped>

</style>