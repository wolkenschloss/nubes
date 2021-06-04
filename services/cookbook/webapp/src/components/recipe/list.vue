<template>
  <div>
    <h2>Alle Rezepte</h2>
    <div>
      <b-card-group columns>
        <b-card v-for="rezept in rezepte"
                v-bind:key="rezept._id"
                :title="rezept.title"
                class="mb-2"
                img-src="https://picsum.photos/600/300/?image=25"
                tag="article">
          <b-card-text>
            {{ rezept.preparation }}
          </b-card-text>
        </b-card>
      </b-card-group>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "list",
  data() {
    return {
      rezepte: {}
    }
  },
  mounted() {
    this.loadRecipes()
  },
  methods: {
    loadRecipes() {
      let uri = "http://localhost:8080/recipe"
      axios.get(uri)
      .then(response => {
        this.rezepte = response.data
        console.log(this.rezepte)
      })
      .catch(error => {
        alert(error)
      })
    }
  }
}
</script>