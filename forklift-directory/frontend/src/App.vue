<template>
  <div id="app">
    <ForkliftDirectory />
  </div>
</template>

<script>
import ForkliftDirectory from './views/ForkliftDirectory.vue'

export default {
  name: 'App',
  components: {
    ForkliftDirectory
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f5f5;
}
</style>
