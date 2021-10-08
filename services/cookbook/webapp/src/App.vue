<template>
  <v-app>
    <v-app-bar app elevation="2">
      <v-app-bar-nav-icon @click="drawer = true"></v-app-bar-nav-icon>
<!--      <v-app-bar-title data-cy="app-title">Cookbook</v-app-bar-title>-->
      <v-toolbar-title data-cy="app-title">Cookbook</v-toolbar-title>
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
    <v-navigation-drawer v-model="drawer" absolute temporary>
      <v-list nav dense>
        <v-list-item-group v-model="group">
          <v-list-item :to="{name: 'contents'}">
            <v-list-item-icon>
              <v-icon>mdi-home</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Table of Contents</v-list-item-title>
          </v-list-item>
          <v-list-item :to="{name: 'ingredients'}">
            <v-list-item-icon>
              <v-icon>mdi-account</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Ingredients</v-list-item-title>
          </v-list-item>
        </v-list-item-group>
      </v-list>
    </v-navigation-drawer>
    <v-main>
       <router-view/>
    </v-main>
  </v-app>
</template>

<script>

export default {
  name: 'App',
  data() {
    return {
      drawer: false,
      group: null
    }
  }
};
</script>
