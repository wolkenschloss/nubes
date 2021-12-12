<template>
  <v-dialog v-model="dialog" max-width="560px">
    <template v-slot:activator="{on, attrs}">
      <v-btn text v-bind="attrs" v-on="on" accesskey="i">Import Recipe</v-btn>
    </template>
    <v-card>
      <v-card-title>
        <span class="text-h5">Import Recipe</span>
      </v-card-title>
      <v-card-text>
        <v-container>
          <v-alert v-if="error" type="error" text>
            {{ error }}
          </v-alert>
          <v-text-field label="URL" required v-model="url"></v-text-field>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-btn text color="primary" @click="importRecipe()" :loading="loading">
          Import
          <v-icon right>mdi-cloud-upload</v-icon>
        </v-btn>
        <v-btn>
          Dismiss
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import axios from "axios";
import {mapActions} from 'vuex'

export default {
  name: "ImportDialog",
  data() {
    return {
      dialog: false,
      url: null,
      loading: false,
      error: null
    }
  },
  methods: {
    ...mapActions('toc', ['queryRecipes']),
    async importRecipe() {
      this.loading = true;
      const job = {order: this.url}
      try {
        const response = await axios.post("/job", job)
        await this.getJobResult(response.headers['location'])
        await this.queryRecipes();
      } catch (error) {
        console.log(error)
      }

      this.loading = false
    },

    // That's evil.
    async getJobResult(location) {
      try {
        console.log(`getJobResult(${location})`)
        const response = await axios.get(location)
        if (response.data.state === "CREATED") {
          console.log("job in progress")
          setTimeout(async () => {
            console.log("await recursive call")
            await this.getJobResult(location)
          }, 100)
        } else {
          console.log(`job completed: ${JSON.stringify(response.data)}`)
          this.loading = false
          this.error = response.data.error
          if (!response.data.error) {
            console.log(`New Recipe added at ${response.data.location}`)
            await this.queryRecipes()
            this.dialog = false
            this.url = null
          }
        }
      } catch (exception) {
        this.loading = false;
        this.error = "Job not available"
        console.log(exception)
      }
    }
  }
}
</script>

<style scoped>
</style>