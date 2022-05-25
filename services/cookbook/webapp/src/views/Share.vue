<template>

  <v-container>
    <v-row class="fill-height" align-content="center" justify="center" v-if="!recipeImported">
      <v-col class="text-subtitle-1 text-center" cols="12">
        Getting your recipe
      </v-col>
      <v-col cols="6">
        <v-progress-linear color="deep-purple accent-4" indeterminate rounded height="6"></v-progress-linear>
      </v-col>
    </v-row>
    <v-col cols="12">
      <v-alert type="error" v-if="errorMessage">
        {{ errorMessage }}
      </v-alert>
      <v-alert type="success" v-if="!errorMessage && recipeImported">
        Recipe '{{ title }}' imported.
      </v-alert>
    </v-col>
<!--    <v-col cols="12">-->
<!--      <ul>-->
<!--        <li>{{this.$props.title}}</li>-->
<!--        <li>{{this.$props.text}}</li>-->
<!--        <li>{{this.$props.url}}</li>-->
<!--        <li>{{this.location}}</li>-->
<!--      </ul>-->
<!--    </v-col>-->
    <v-btn :to="{name: 'contents'}">Table of Contents</v-btn>

  </v-container>
</template>

<script>
import {Resource} from "@/store/modules/resource";
import axios from "axios";
import {mapActions} from "vuex";

const resource = new Resource("{+baseUrl}/job/{id}")
const recipeResource = new Resource("{+baseUrl}")

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

export default {
  name: "Share",
  props: {
    title: String,
    text: String,
    url: String
  },
  data() {
    return {
      recipeImported: false,
      errorMessage: null,
      location: null
    }
  },
  async mounted() {
    try {
      console.log("Component Share.vue is mounted")
      console.log("Parameter text: ", this.$props.text)
      console.log("Parameter title: ", this.$props.title)
      const job = {order: this.$props.text}
      console.log("POST:", job)
      // return
      const response = await axios.post(resource.url(), job)
      const jobLocation = new URL(response.headers['location'])
      const path = jobLocation.pathname
      await delay(1000)
      const location = await this.getJobResult(path, 1)
      console.log("recipe created at location ", location)
      this.recipeImported = true
    } catch (error) {
      console.log("mounted error: ", error)
      this.errorMessage = `Error while reading ${this.$props.title} from source.`
    }
    console.log("recipe imported")
  },
  methods: {
    ...mapActions('toc', ['queryRecipes']),
    async getJobResult(location, retriesLeft) {
      if (retriesLeft === 0) {
        throw Error("Erfolglos")
      }
      console.log(`getJobResult(${location})`)
      const response = await axios.get(location)
      if (response.data.state === "CREATED") {
        throw Error("Import nicht fertig.")
      } else {
        console.log(`job completed: ${JSON.stringify(response.data)}`)
        this.loading = false
        this.error = response.data.error
        if (!response.data.error) {
          console.log(`New Recipe added at ${response.data.location}`)
          await this.queryRecipes()
          return response.data.location
        }
      }
    }
  }
}

</script>

<style scoped>

</style>