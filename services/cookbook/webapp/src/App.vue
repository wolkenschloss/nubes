<template>
  <v-app>
    <v-app-bar app elevation="2">
      <v-app-bar-title data-cy="app-title">Cookbook</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-switch
        v-model="$vuetify.theme.dark"
        hide-details
        title="toggle light and dark theme">
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
