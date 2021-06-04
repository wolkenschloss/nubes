<template>
  <div id="app">
    <b-navbar toggleable="lg" type="dark" variant="dark">
      <b-navbar-brand href="#">MyCloud</b-navbar-brand>
      <b-navbar-toggle target="nav-collaps"></b-navbar-toggle>
      <b-collapse id="nav-collaps" is-nav>
        <b-navbar-nav>
          <b-nav-item href="#">Home</b-nav-item>
        </b-navbar-nav>
        <b-navbar-nav class="ml-auto">
          <b-nav-form>
            <b-form-input class="mr-sm-2" placeholder="Suche" size="sm"></b-form-input>
            <b-button class="my-2 my-sm-0" size="sm" type="submit">Suchen</b-button>
          </b-nav-form>
        </b-navbar-nav>

      </b-collapse>
    </b-navbar>
    <b-container fluid="lg">
    <div>
      <h1>Kochbuch</h1>
      <p>Rezepte für Alle und Keinen</p>
    </div>
    </b-container>
  </div>
</template>

<script>

export default {
  name: 'App',
  components: {

  }
}
</script>

<style>
</style>
