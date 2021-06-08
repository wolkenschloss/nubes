<template>
  <div>
    <div class="d-flex justify-content-between flex-md-row flex-column-reverse align-items-md-start mb-3">
      <div >
        <h2>{{ recipe.title }} </h2>
      </div>

      <b-button-toolbar class="d-flex mb-2 mb-md-0">
        <b-button :to="{name: 'edit', params: {id: this.$route.params.id}}" variant="outline-primary">
          <b-icon-pencil></b-icon-pencil> Bearbeiten</b-button>
        <b-button variant="outline-danger" v-on:click="remove">
          <b-icon-trash></b-icon-trash> Löschen</b-button>
        <b-button to="/create" variant="link">
          <b-icon-plus></b-icon-plus> Neues Rezept</b-button>
      </b-button-toolbar>
    </div>
    <hr/>
    <div>
      {{ recipe.preparation }}
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "details",
  props: {id: String},
  data() {
    return {
      recipe: {title: "", preparation: ""}
    }
  },
  mounted() {
    this.load();
  },
  methods: {
    load() {
      let url = "/recipe/" + this.$props.id
      axios.get(url)
      .then(response => {
        this.recipe = response.data
        console.log(JSON.stringify(this.recipe))
      })
      .catch(error => {
        alert(error)
      })
    },
    remove: function() {
      let url = "/recipe/" + this.$props.id
      axios.delete(url)
      .then(() => {
        this.$bvToast.toast('Recipe deleted', {
          title: 'Deleted',
          variant: 'danger',
          noAutoHide: true,
          appendToast: true
        })
        this.$router.push("/")
      })
      .catch(() => {
        this.$bvToast.toast("Recipe not deleted", {
          title: 'Error',
          variant: 'danger',
          noAutoHide: true,
          appendToast: true
        })
      })
    }
  }
}

</script>

<style scoped>

</style>