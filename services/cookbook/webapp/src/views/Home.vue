<template>
  <div>
    <div class="d-flex justify-content-between flex-column-reverse flex-md-row align-items-end mb-3">

      <div class="d-flex d-md-block justify-content-between flex-md-grow-1 align-self-stretch" role="search">
        <div class="mr-md-2 flex-grow-1">
          <b-form class="w-100">
            <b-input-group>
              <b-input-group-prepend>
                <b-dropdown disabled text="Filter">
                  <b-dropdown-item>Vorspeisen</b-dropdown-item>
                  <b-dropdown-item>Hauptspeisen</b-dropdown-item>
                  <b-dropdown-item>Nachspeisen</b-dropdown-item>
                </b-dropdown>
              </b-input-group-prepend>
              <b-form-input class="width-full" disabled placeholder="Rezept suchen" type="search"></b-form-input>
              <b-input-group-append>
                <b-button disabled type="submit">
                  <b-icon-search></b-icon-search> Suchen
                </b-button>
              </b-input-group-append>
            </b-input-group>
          </b-form>
        </div>
      </div>

      <div class="d-flex justify-content-between align-self-stretch mb-2 mb-md-0" role="navigation">
        <div class="">
          <b-button-group>
            <b-button disabled variant="outline-secondary">Lorem</b-button>
            <b-button disabled variant="outline-secondary">Ipsum</b-button>
          </b-button-group>
        </div>
        <div class="ml-md-2">
          <b-button to="/create" variant="primary">Neues Rezept</b-button>
        </div>
      </div>
    </div>

    <list></list>
  </div>
</template>

<script>

import List from "@/components/recipe/list";

export default {
  name: 'App',
  components: {
    List
  }
}
</script>

<style>

</style>
