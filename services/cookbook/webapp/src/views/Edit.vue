<template>
<div>
  <h2>Edit</h2>
  <b-form @submit.prevent="onSubmit">
    <b-form-group id="title-group"
                  v-model="recipe.title"
                  description="Titel des Rezepts"
                  label="Titel"
                  label-for="title-input">
      <b-form-input id="title-input"
                    ref="title"
                    v-model="recipe.title"
                    autocomplete="off"
                    placeholder="Titel"
                    required
                    type="text"/>
    </b-form-group>
    <b-form-group id="preparation-group"
                  description="Beschreibung, wie das Rezept zubereitet wird"
                  label="Zubereitung"
                  label-for="preparation-input">
      <b-form-textarea id="preparation-input"
                       ref="preparation"
                       v-model="recipe.preparation"
                       placeholder="Zubereitung des Rezepts beschreiben..."
                       required rows="6">

      </b-form-textarea>
    </b-form-group>
    <b-button type="submit" variant="primary">OK</b-button>&nbsp;
    <b-button :to="{name: 'details', params: {id: this.$props.id}}" variant="secondary">Cancel</b-button>
  </b-form>
</div>
</template>

<script>
import axios from "axios";

export default {
  name: "Edit",
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