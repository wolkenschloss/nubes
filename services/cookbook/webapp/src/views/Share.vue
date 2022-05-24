<template>
  <v-container>
    <p>
      {{title}}
    </p>
    <p>
      {{text}}
    </p>
    <p>
      {{url}}
    </p>
  </v-container>
</template>

<script>
export default {
  name: "Share",
  props: {
    title: String,
    text: String,
    url: String
  },
  mounted() {
    console.log("Component Share.vue is mounted")
  }
}

</script>

<style scoped>

</style>