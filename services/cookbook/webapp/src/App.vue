<template>
  <div id="app">
    <b-navbar toggleable="lg" type="dark" variant="dark">
      <b-navbar-brand href="#">Wolkenschloss</b-navbar-brand>
      <b-navbar-toggle target="nav-collaps"></b-navbar-toggle>
      <b-collapse id="nav-collaps" accordion="" is-nav>
        <b-navbar-nav>
          <b-nav-item :to="{name: 'kochbuch'}" exact exact-active-class="active">Kochbuch</b-nav-item>
          <b-nav-item :to="{name: 'wochenplan'}" exact exact-active-class="active">Wochenplan</b-nav-item>
          <b-nav-item exact exact-active-class="active" to="/about">About</b-nav-item>
        </b-navbar-nav>
        <b-navbar-nav class="ml-auto">
          <b-nav-form>
            <b-form-input class="mr-sm-2" placeholder="Suche" size="sm"></b-form-input>
            <b-button class="my-2 my-sm-0" size="sm" type="submit">Suchen</b-button>
          </b-nav-form>
          <b-nav-item href="https://www.github.com/wolkenschloss" target="_blank">
            <b-icon-github></b-icon-github>
          </b-nav-item>
        </b-navbar-nav>
      </b-collapse>
    </b-navbar>
<!--    <b-nav small style="background-color: blanchedalmond" class="nav-">-->
<!--      <b-nav-text>Kochbuch</b-nav-text>-->
<!--      <b-nav-item to="/create">Neues Rezept</b-nav-item>-->
<!--    </b-nav>-->
    <b-container class="mt-2" fluid="lg">
      <div>
        <h1>Kochbuch</h1>
        <p>Rezepte für Alle und Keinen</p>
      </div>
      <b-nav>
        <router-view name="xnav"></router-view>
      </b-nav>
      <transition mode="out-in" name="fade">
        <router-view class="view"/>
      </transition>
    </b-container>

  </div>
</template>

<script>
export default {
}

</script>

<style lang="scss">

.fade-enter-active, .fade-leave-active {
  transition: opacity .25s ease;
}
.fade-enter, .fade-leave-active {
  opacity: 0;
}
</style>
