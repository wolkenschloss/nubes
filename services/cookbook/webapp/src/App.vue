<template>
  <v-app>
    <v-app-bar app color="primary" dark>
      <v-app-bar-title data-cy="app-title">Wolkenschloss</v-app-bar-title>
    </v-app-bar>

    <v-main>
      <v-scale-transition>
        <router-view/>
      </v-scale-transition>
    </v-main>
  </v-app>
</template>

<script>

export default {
  name: 'App',
};
</script>
