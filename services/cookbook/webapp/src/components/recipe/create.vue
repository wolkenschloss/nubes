<template>
  <div>
    <h2>Neues Rezept eingeben</h2>
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
                         placeholder="Zubereitung des Rezepts beschreiben..."
                         required
                         v-model="recipe.preparation" rows="6">

        </b-form-textarea>
      </b-form-group>
      <b-button type="submit" variant="primary">OK</b-button>
    </b-form>
  </div>
</template>

<script>
import axios from "axios";

export default {

  name: "create",
  data() {
    return {
      recipe: {}
    }
  },
  methods: {
    onSubmit() {
      axios.post('/recipe', this.recipe)
      .then(result => {
        console.log("Rezept abgeschickt. Ergebnis ist")
        console.log(JSON.stringify(result))
        this.makeToast('success')
        this.recipe = {}
      })
      .catch(error => alert(error))
    },
    makeToast(variant = null) {
      this.$bvToast.toast('Toast body content', {
        title: `Variant ${variant || 'default'}`,
        variant: variant,
        solid: true,
        toaster: 'b-toaster-bottom-full',
        appendToast: true
      })
    }
  }
}
</script>

<style scoped>

</style>