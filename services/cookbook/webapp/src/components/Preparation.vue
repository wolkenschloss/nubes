<template>
  <v-card-text v-if="text">
    <p v-for="(p, i) in paragraphs" :key="i">
      {{ p }}
    </p>
  </v-card-text>
</template>

<script>
export default {
  name: "Preparation",
  props: ['text'],
  computed: {
    paragraphs() {
      return this.text ? this.text.split("\n\n") : []
    }
  }
}

</script>

<style scoped>

</style>