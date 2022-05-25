<template>
  <v-dialog v-model="dialog" max-width="560px">
    <template v-slot:activator="{on, attrs}">
      <v-btn text v-bind="attrs" v-on="on" v-shortcut="'i'">Import Recipe</v-btn>
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
          <v-text-field label="URL" required v-model="url" autofocus></v-text-field>
        </v-container>
      </v-card-text>
      <v-card-actions>
        <v-btn text color="primary" :loading="loading" :to="{name: 'share-target', query: {text: this.url, title: 'default title'}}">
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
}
</script>

<style scoped>
</style>