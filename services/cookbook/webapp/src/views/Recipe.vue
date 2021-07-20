<template>
  <v-card class="ma-8">
    <v-card-title v-text="recipe.title"></v-card-title>
    <v-card-text v-text="recipe.preparation"></v-card-text>
    <v-card-actions>
      <v-btn text v-on:click="remove" >
        <v-icon>mdi-delete</v-icon>
        Delete
      </v-btn>
    </v-card-actions>
    <create fab-icon="mdi-pencil" title="Edit Recipe" v-on:change="save" v-bind:value="copy" v-on:cancel="cancel"></create>
  </v-card>
</template>

<script>
import axios from "axios";
import Create from "@/views/Create";

export default {
  name: "Recipe",
  components: {Create},
  props: {id: String},
  data() {
    return {
      recipe: {},
      copy: {}
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
        this.copy = {...response.data}
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
    },
    async save(r) {
      this.recipe = r
      let url = "/recipe/" + this.$props.id
      try {
        await axios.put(url, r)
        this.recipe = {...r}
      } catch (error) {
        alert(error)
      }
    },
    cancel() {
      this.copy = {...this.recipe}
    }
  }
}
</script>

<style scoped>

</style>