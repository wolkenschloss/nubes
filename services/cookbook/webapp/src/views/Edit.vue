<template>
<div>
  <h2>Rezept bearbeiten</h2>
  <b-form @submit.prevent="onSubmit">
    <editor v-bind:recipe="this.$data.recipe"></editor>
    <hr/>
    <b-button-toolbar aria-label="Edit Recipe Actions" key-nav>
      <b-button-group>
        <b-button type="submit" variant="primary">
          <b-icon></b-icon>Änderungen speichern</b-button>&nbsp;
        <b-button :to="{name: 'details', params: {id: this.$props.id}}" variant="secondary" >Cancel</b-button>
      </b-button-group>
    </b-button-toolbar>
  </b-form>
</div>
</template>

<script>
import axios from "axios";
import Editor from "@/components/recipe/editor";

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
        this.$bvToast.toast('Änderungen gespeichert', {
          title: 'Rezept',
          variant: 'success',
          toaster: 'b-toaster-bottom-full',
          appendToast: true
        })
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