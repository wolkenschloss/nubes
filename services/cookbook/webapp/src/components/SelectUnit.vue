<template>
  <v-select dense
            :items="entries"
            label="Unit"
            v-bind:value="internal"
            @change="$emit('change', $event)"
            @input="$emit('input', $event)"
            clearable
            @click:clear="clear"
            ref="units"
            type="text">
    <template v-slot:selection="{item}">
      <span>{{ item.value }}</span>
    </template>
  </v-select>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: "SelectUnit",
  props: ['value'],
  data() {
    return {
      internal: this.value
    }
  },
  computed: {
    ...mapGetters('units', ['entries'])
  },
  methods: {
    clear() {
      this.internal = ''
    },
    selectItem(val) {
      this.$refs.units.selectItem(val)
    }
  }
}
</script>

<style scoped>

</style>