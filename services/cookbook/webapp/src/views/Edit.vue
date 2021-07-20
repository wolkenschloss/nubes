<template>
    <v-dialog v-model="dialog" :fullscreen="$vuetify.breakpoint.mobile" scrollable max-width="560px" max-height="560px">
      <template v-slot:activator="{on, attrs}">
        <v-btn color="primary" elevation="2" fab bottom left fixed v-bind="attrs" v-on="on">
          <v-icon>{{fabIcon}}</v-icon>
        </v-btn>
      </template>
    <v-card class="mx-auto">
      <v-toolbar dark color="primary" class="flex-grow-0">
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
        <v-toolbar-title>{{title}}</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-toolbar-items>
          <v-btn dark text @click="save">Save</v-btn>
        </v-toolbar-items>
      </v-toolbar>
      <editor v-model="value" class="pa-6"></editor>
    </v-card>
    </v-dialog>
</template>

<script>
import axios from "axios";
import Editor from "../components/Editor";

export default {
  name: "Edit",
  props: ['fabIcon', 'title', 'value'],
  components: {Editor},
  data() {
    return {
      dialog: false,
      editable: this.$props.value || {}
    }
  },
  methods: {
    closeDialog() {
      this.$emit('cancel')
      this.dialog = false
    },
    save() {
      this.$emit('change', this.value)
      this.dialog = false;
    },
  }
}

</script>

<style scoped>

</style>