<template>
  <v-container>
    <v-card>
      <v-card-title>Rezept bearbeiten</v-card-title>
      <v-form @submit.prevent="onSubmit">
        <editor v-bind:recipe="this.$data.recipe"></editor>
        <v-btn text type="submit" class="success mt-3 mr-3">Save</v-btn>
        <v-btn text class="secondary mr-3 mt-3" :to="{name: 'details', params: {id: this.$props.id}}">Cancel</v-btn>
      </v-form>
    </v-card>
  </v-container>

</template>

<script>
import Editor from "../components/Editor";
import axios from "axios";

export default {
  name: "Edit",
  components: {Editor},
  props: {id: String},
  data() {
    return {
      recipe: {}
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    load() {
      let url = "/recipe/" + this.$props.id;
      axios.get(url)
          .then(response => {
            this.recipe = response.data
          })
          .catch(error => {
            alert(error)
          })
    },
    onSubmit() {
      let url = '/recipe/' + this.$props.id;
      axios.put(url, this.recipe)
          .then(response => {
            this.recipe = response.data
            this.$router.push({name: 'details', params: { id: this.$props.id }})
          })
          .catch(error => {alert(error)})
    }
  }
}

</script>

<style scoped>

</style>