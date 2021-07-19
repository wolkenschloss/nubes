<template>
  <v-form>
    <v-text-field label="Rezeptname" v-model="recipe.title" prepend-icon="mdi-folder"></v-text-field>
    <v-textarea label="Rezeptzubereitung" v-model="recipe.preparation" prepend-icon="mdi-pencil"></v-textarea>
  </v-form>
</template>

<script>
export default {
  name: "Editor",
  props: ['recipe']
}
</script>

<style scoped>

</style>