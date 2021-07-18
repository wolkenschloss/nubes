<template>
  <v-container>
    <v-sheet>
      <h2>Neues Rezept eingeben</h2>
      <v-form @submit.prevent="onSubmit">
        <editor v-bind:recipe="this.$data.recipe"></editor>
        <v-btn text type="submit">Save</v-btn>
        <v-btn text to="/"><v-icon>mdi-close</v-icon></v-btn>
      </v-form>
    </v-sheet>
  </v-container>
</template>

<script>
import axios from "axios";
import Editor from "../components/Editor";

export default {
  name: "Create",
  components: {Editor},
  data() {
    return {
      recipe: {}
    }
  },

  methods: {
    onSubmit() {
      axios.post("/recipe", this.recipe)
      .then(() => {
        this.$router.push("/")
      })
      .catch(error => alert(error))
    }
  }
}

</script>

<style scoped>

</style>