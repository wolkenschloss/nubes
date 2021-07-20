<template>
  <v-form>
    <v-text-field label="Rezeptname" v-model="model.title" prepend-icon="mdi-folder"></v-text-field>
    <v-textarea label="Rezeptzubereitung" v-model="model.preparation" prepend-icon="mdi-pencil"></v-textarea>
  </v-form>
</template>

<script>
export default {
  name: "Editor",
  props: ['value'],
  computed: {
    model: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    }
  }
}

</script>

<style scoped>

</style>