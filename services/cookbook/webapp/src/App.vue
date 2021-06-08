<template>
  <div id="app">
    <b-navbar toggleable="lg" type="dark" variant="dark">
      <b-navbar-brand><b-icon-cloud></b-icon-cloud> Wolkenschloss</b-navbar-brand>
      <b-navbar-toggle target="nav-collaps"></b-navbar-toggle>
      <b-collapse id="nav-collaps" accordion="" is-nav>
        <b-navbar-nav>
          <b-nav-item :to="{name: 'kochbuch'}" exact exact-active-class="active">Kochbuch</b-nav-item>
          <b-nav-item :to="{name: 'wochenplan'}" disabled exact exact-active-class="active">Wochenplan</b-nav-item>
        </b-navbar-nav>
        <b-navbar-nav class="ml-auto">
          <b-nav-item href="https://www.github.com/wolkenschloss" target="_blank">
            <b-icon-github></b-icon-github>
          </b-nav-item>
        </b-navbar-nav>
      </b-collapse>
    </b-navbar>
    <b-container class="mt-3" fluid="lg">
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
