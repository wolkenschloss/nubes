<template>
  <div>
    <h2>{{recipe.title}}</h2>
    <div>{{recipe.preparation}}</div>
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
    }
  }
}

</script>

<style scoped>

</style>