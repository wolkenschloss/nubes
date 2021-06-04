<template>
  <div id="app">
    <b-container fluid="lg">
      <b-navbar fixed="top" toggleable="lg" type="dark" variant="dark">
        <b-navbar-brand href="#">MyCloud</b-navbar-brand>
        <b-navbar-toggle target="nav-collaps"></b-navbar-toggle>
        <b-collapse id="nav-collaps" is-nav>
          <b-nav-item href="#">Home</b-nav-item>
        </b-collapse>
      </b-navbar>
      <div id="main">
        <img alt="Vue logo" src="./assets/logo.png">
        <HelloWorld msg="Welcome to Your Vue.js App"/>
      </div>
    </b-container>
  </div>
</template>

<script>
import HelloWorld from './components/HelloWorld.vue'

export default {
  name: 'App',
  components: {
    HelloWorld
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
