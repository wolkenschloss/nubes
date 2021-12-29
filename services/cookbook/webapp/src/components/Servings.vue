<template>
  <v-text-field label="Servings"
                :value="value"
                @input="updateValue"
                type="number"
                persistent-hint
                :rules="servingRules"
                autofocus
                required
                :hint="hint"/>
</template>

<script>

export default {
  inheritAttrs: true,
  name: "Servings",
  props: ['value', 'hint'],
  data() {
    return {
      servingRules: [
        value => (!!value && value > 0 && value < 101) || "The number of servings must be between 1 and 100"
      ],
    }
  },
  methods: {
    updateValue(value) {
      console.log(`updateValue(${value})`)
      this.$emit('input', value)
    }
  }
}
</script>

<style scoped>

</style>