<template>
  <b-row>
    <b-col lg="8">
      <list></list>
    </b-col>
    <b-col lg="4">
      <create></create>
    </b-col>
  </b-row>
</template>

<script>
import Create from "@/components/recipe/create";
import List from "@/components/recipe/list";

export default {
  name: 'App',
  components: {
    Create, List
  }
}
</script>
