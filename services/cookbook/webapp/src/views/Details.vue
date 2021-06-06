<template>
  <div>
    <h2>{{recipe.title}} </h2>
    <div>
        {{ recipe.preparation }}
    </div>
    <hr/>
    <b-button :to="{name: 'edit', params: {id: this.$route.params.id}}" variant="primary">Edit</b-button>&nbsp;
    <b-button variant="danger" v-on:click="remove">Delete</b-button>
  </div>
</template>

<script>
import axios
  from "axios";

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