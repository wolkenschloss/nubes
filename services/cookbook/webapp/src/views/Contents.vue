<template>
  <v-container>
    <v-toolbar elevation="0">
<!--       v-shortcut="'n'"-->
      <v-btn color="secondary"  @click="edit({ingredients: [], servings: 1})">
        New Recipe
      </v-btn>
<!--      @change="create"-->
<!--      @cancel="edit"-->
<!--      v-bind:value.sync="copy"-->
<!--      @input="input"-->
      <edit fab-icon="mdi-plus" title="New Recipe" v-model:value="recipe" />&nbsp;
      <import-dialog/>&nbsp;
<!--      v-shortcut:focus="['ctrl', 'f']"-->
      <v-text-field v-model="search"

                    append-icon="mdi-magnify"
                    label="Search"
                    single-line
                    hide-details
                    clearable/>
    </v-toolbar>
    <v-data-table
        class="mt-4"
        hide-default-header
        :items-per-page="$vuetify.breakpoint.mobile ? 5 : 10"
        :footer-props="{'items-per-page-options': [5, 10, 15, 20]}"
        :loading="loading"
        :items="toc"
        :headers="headers"
        :options.sync="pagination"
        :server-items-length="total"
        item-key="recipeId"
        @click:row="onItemClick"/>
  </v-container>
</template>

<script>

import Edit from "@/views/Edit";
import {debounce, isEqual} from "lodash";
import {mapActions, mapGetters} from 'vuex'
import ImportDialog from "@/components/ImportDialog";

export default {
  name: 'Contents',
  components: {ImportDialog, Edit},
  directives: {
    shortcut: {
      bind(el, binding, vnode) {
        console.log(`binding shortcut for ${vnode.tag}: ${binding.arg} ${JSON.stringify(binding.modifiers)} ${binding.value}`)
        el._handler = e => {

          function isMatching(event) {
            const { key, ctrlKey, altKey, shiftKey, metaKey } = event

            if (Array.isArray(binding.value)) {
              const result = binding.value.filter(element => {
                return (ctrlKey && element === 'ctrl')
                    || (altKey && element === 'alt')
                    || element === key
              })
              return isEqual(result, binding.value)
            } else {
              console.log(`compare single character: ${key} === ${binding.value}`)
              return key === binding.value
            }
          }

          if (isMatching(e)) {
            if (binding.arg === 'focus') {
              console.log(`applies to  ${el.tagName}`)
              let input = el.querySelector('input')
              input.focus()
            } else {
              const {nodeName, isContentEditable} = document.activeElement
              if (isContentEditable) return
              if (['INPUT', 'TEXTAREA', 'SELECT'].includes(nodeName)) return

              console.log(`simulate button click`)
              function emit(vnode, name, data) {
                var handlers = (vnode.data && vnode.data.on) || (vnode.componentOptions && vnode.componentOptions.listeners);
                if (handlers && handlers[name]) {
                  handlers[name].fns(data)
                }
              }

              emit(vnode, 'click', {})
            }

            e.preventDefault()
            // e.stopImmediatePropagation()
            e.stopPropagation()
          }
        }

        document.addEventListener('keydown', el._handler)
      },
      unbind(el, binding) {
        console.log(`unbinding shortcut ${binding.value}`)
        document.removeEventListener('keydown', el._handler)
      }
    }
  },
  watch: {
    pagination: {
      async handler() {
        await this.load()
      },
      deep: true
    },
    search: {
      async handler() {
        await this.load()
      },
      deep: true
    }
  },
  computed: {
    ...mapGetters('toc', ['toc', 'total']),

    recipe: {
      get() {
        return this.$store.getters['recipe/copy']
      },
      set(value) {
        console.log(`!!!! contents set copy ${JSON.stringify(value)}`)
        if(value) {
          this.create(value)
        }
        this.edit(null)
        // this.$store.commit('recipe/setCopy', value)
      }
    },
    pagination: {
      get() {
        return this.$store.getters['toc/pagination']
      },
      set(value) {
        this.$store.commit('toc/setPagination', value)
      }
    },
    search: {
      get() {
        return this.$store.getters['toc/filter']
      },
      set: debounce(async function (value) {
        await this.$store.commit('toc/setFilter', value)
      }, 500)
    }
  },
  data() {
    return {
      loading: false,
      headers: [
        {text: "Recipe", value: 'title'},
      ]
    }
  },
  created() {
    this.newRecipe()
  },
  mounted() {
    console.log("Mounted")
  },

  methods: {
    ...mapActions('toc', ["updateQuery", "queryRecipes"]),
    ...mapActions('recipe', ["newRecipe", 'createRecipe', 'edit', 'create']),

    async load() {
      this.loading = true
      await this.queryRecipes()
      this.loading = false
    },
    onItemClick(item) {
      this.$router.push({name: 'details', params: {id: item['recipeId']}})
    },
    // async change(recipe) {
    //   console.log(`contents change: ${JSON.stringify(recipe)}`)
    //   this.edit(null)
    //   await this.createRecipe(recipe)
    //   this.newRecipe()
    //   this.queryRecipes()
    // },
    // input(param) {
    //   console.log(`contents input: ${JSON.stringify(param)}`)
    // }
  }
}
</script>
