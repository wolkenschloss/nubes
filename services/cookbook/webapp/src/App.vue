<template>
  <v-app>
    <v-app-bar app elevation="2">
      <v-app-bar-title data-cy="app-title">Cookbook</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-switch
          color="secondary"
          v-model="$vuetify.theme.dark"
          hide-details
          title="toggle light and dark theme">
        <template v-slot:label>
          <v-icon>mdi-brightness-6</v-icon>
        </template>
      </v-switch>
    </v-app-bar>
    <v-main>
      <router-view/>
    </v-main>
  </v-app>
</template>

<script>

export default {
  name: 'App',
};
</script>
