<template>
  <div>
    <v-text-field label="Rezeptname" v-model="recipe.title" prepend-icon="mdi-folder"></v-text-field>
    <v-textarea solo label="Rezeptzubereitung" v-model="recipe.preparation" prepend-icon="mdi-pencil"></v-textarea>
  </div>
</template>

<script>
export default {
  name: "Editor",
  props: ['recipe']
}
</script>

<style scoped>

</style>