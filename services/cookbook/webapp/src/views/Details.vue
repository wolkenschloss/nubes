<template>
<v-container>
    <v-card class="ma-4 pa-4" flat>
      <v-card-title v-text="recipe.title"></v-card-title>
      <preparation :text="recipe.preparation"></preparation>
      <v-list v-if="recipe.ingredients.length > 0">
        <v-list-group>
          <template v-slot:activator>
            <v-list-item-icon>
              <v-icon>mdi-food</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Ingredients</v-list-item-title>
          </template>
          <v-list-item v-for="(ingredient, index) in recipe.ingredients" :key="index">
            <v-list-item-content>
              <v-list-item-title>
                {{ingredient.quantity}} {{ingredient.unit}} {{ingredient.name}}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-list-group>
      </v-list>

      <v-card-actions>
        <v-btn text v-on:click="remove">
          <v-icon>mdi-delete</v-icon>
          Delete
        </v-btn>
      </v-card-actions>
      <edit fab-icon="mdi-pencil" title="Edit Recipe" v-on:change="save" v-bind:value="copy" v-on:cancel="cancel"/>
    </v-card>
</v-container>
</template>

<script>
import axios from "axios";
import Edit from "@/views/Edit";
import Preparation from "@/components/Preparation";

export default {
  name: "Recipe",
  components: {Preparation, Edit},
  props: {id: String},
  data() {
    return {
      recipe: {
        ingredients: []
      },
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