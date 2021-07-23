<template>
  <v-app>
    <v-app-bar app color="primary" dark>
      <v-app-bar-title data-cy="app-title">Wolkenschloss</v-app-bar-title>
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
