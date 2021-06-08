<template>
  <div>
    <h2>Neues Rezept eingeben</h2>
    <b-form @submit.prevent="onSubmit">
      <editor v-bind:recipe="this.$data.recipe"></editor>
      <hr/>
      <b-button-toolbar>
        <b-button-group>
          <b-button type="submit" variant="primary">OK</b-button>
        </b-button-group>
        <b-button-group>
          <b-button to="/" type="button" variant="secondary">Cancel</b-button>
        </b-button-group>
      </b-button-toolbar>
    </b-form>
  </div>
</template>

<script>
import axios from "axios";
import editor from '@/components/recipe/editor'

export default {
  name: "Create",
  components: {editor},
  data() {
    return {
      recipe: {}
    }
  },
  methods: {
    onSubmit() {
      axios.post('/recipe', this.recipe)
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