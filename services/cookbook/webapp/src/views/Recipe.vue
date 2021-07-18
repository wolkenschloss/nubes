<template>
  <v-container>
  <v-card>
    <v-card-title v-text="recipe.title"></v-card-title>

    <v-card-text v-text="recipe.preparation"></v-card-text>
    <v-card-actions>
      <v-btn text class="primary" :to="{name: 'edit', params: {id:this.$props.id}}">Edit</v-btn>
      <v-btn test class="secondary" v-on:click="remove">Delete</v-btn>
    </v-card-actions>
  </v-card>
  </v-container>
</template>

<script>
import axios from "axios";

export default {
  name: "Recipe",
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
      let url = "/recipe/" + this.$props.id
      axios.get(url)
      .then(response => {
        this.recipe = response.data
      })
      .catch(error => {
        alert(error)
      })
    },
    remove() {
      let url = "/recipe/" + this.$props.id
      axios.delete(url)
          .then(() => {
            this.$router.push("/")
          })
          .catch(error => {
            alert(error)
          })
    }
  }
}
</script>

<style scoped>

</style>