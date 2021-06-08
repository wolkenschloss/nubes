<template>
  <div>
      <b-form-group id="title-group"
                    label="Rezeptname"
                    label-for="title-input">
        <b-form-input id="title-input"
                      ref="title"
                      v-model="recipe.title"
                      autocomplete="off"
                      placeholder="z.B. Frittierter Salzreis der Vollendung"
                      required
                      type="text"/>
      </b-form-group>
      <b-form-group id="preparation-group"
                    description="Hier kannst Du beschreiben, welche Schritte für die Zubereitung notwendig sind."
                    label="Rezeptzubereitung"
                    label-for="preparation-input">
        <b-form-textarea id="preparation-input"
                         ref="preparation"
                         placeholder="Zubereitung des Rezepts beschreiben..."
                         required
                         v-model="recipe.preparation" rows="6">

        </b-form-textarea>
      </b-form-group>
  </div>
</template>

<script>

export default {

  name: "editor",
  props: ['recipe']
}
</script>

<style scoped>

</style>