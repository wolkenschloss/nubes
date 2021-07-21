<template>
  <v-list>
    <v-list-item-group>
      <template v-for="(recipe, index) in recipes">
        <v-list-item :key="recipe.recipeId" :to="{name: 'details', params: {id: recipe.recipeId}}">
            <v-list-item-content :key="recipe.recipeId">
              <v-list-item-title v-text="recipe.title" :key="recipe.recipeId"></v-list-item-title>
            </v-list-item-content>
        </v-list-item>
        <v-divider :key="recipe.recipeId" v-if="index < recipes.length -1"></v-divider>
      </template>
    </v-list-item-group>
  </v-list>
</template>

<script>
export default {
  name: "List",
  props: ['recipes']
}

</script>

<style scoped>

</style>