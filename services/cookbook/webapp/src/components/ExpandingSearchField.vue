<template>
  <v-scroll-x-transition>
      <v-text-field v-model="content"
                    v-if="expanded"
                    prepend-icon="mdi-magnify"
                    placeholder="Searching"
                    single-line
                    hide-details
                    clearable
                    autofocus
                    :ripple="false"
                    @click:clear="handleClear"
                    @input="handleInput"
                    @blur="expanded = content"
      />
      <v-btn v-else icon>
        <v-icon @click="expanded = ! expanded">mdi-magnify</v-icon>
      </v-btn>
  </v-scroll-x-transition>
</template>

<script>
export default {
  name: "ExpandingSearchField",
  props: ['value'],
  data() {
    return {
      expanded: false,
      content: this.value
    }
  },
  watch: {
    expanded(newValue) {
      this.$emit('resize', newValue)
    }
  },
  methods: {
    handleInput() {
      console.log("emit input", this.content)
      this.$emit('input', this.content)
    },
    handleClear() {
      console.log("handle clear")
      this.expanded = false
      this.content = null
      this.$emit('input', this.content)
    }
  }
}
</script>

<style scoped >

</style>