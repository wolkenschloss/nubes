<template>
  <div>
    <h2>Create</h2>
    <b-form @submit.prevent="onSubmit">
      <editor v-bind:recipe="this.$data.recipe"></editor>
      <b-button type="submit" variant="primary">OK</b-button>&nbsp;
      <b-button to="/" type="button" variant="secondary">Cancel</b-button>
    </b-form>
    <hr/>
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