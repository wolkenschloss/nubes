<template>
  <v-form>
    <v-text-field label="Title" v-model="model.title" prepend-icon="mdi-folder"></v-text-field>
    <v-textarea label="Preparation" v-model="model.preparation" prepend-icon="mdi-pencil"></v-textarea>
    <v-list>
      <v-list-item-group>
        <v-subheader>Ingredients</v-subheader>
        <v-list-item v-for="(ingredient, index) in model.ingredients" :key="index" link @click="editIngredient(index)">
          <template v-slot:default="{active}">
            <v-list-item-content>
              <v-list-item-title>
                {{ ingredient.name }}
              </v-list-item-title>

            </v-list-item-content>
            <v-list-item-action>
              <v-list-item-action-text>
                {{ ingredient.quantity }} {{ ingredient.unit }}
              </v-list-item-action-text>
            </v-list-item-action>
<!--            <v-list-item-action v-if="active">-->
<!--              <v-menu bottom left>-->
<!--                <template v-slot:activator="{on, attr}">-->
<!--                  <v-btn icon v-bind="attr" v-on="on">-->
<!--                    <v-icon>mdi-dots-vertical</v-icon>-->
<!--                  </v-btn>-->
<!--                </template>-->
<!--                <v-list>-->
<!--                  <v-list-item @click="editIngredient(index)">-->
<!--                    <v-list-item-title>Edit</v-list-item-title>-->
<!--                  </v-list-item>-->
<!--                  <v-list-item>-->
<!--                    <v-list-item-title @click="deleteIngredient(index)">Delete</v-list-item-title>-->
<!--                  </v-list-item>-->
<!--                </v-list>-->
<!--              </v-menu>-->
<!--            </v-list-item-action>-->
          </template>
        </v-list-item>
        <v-divider></v-divider>
        <v-list-item>
          <v-list-item-title>
            <v-btn @click="addIngredient">Add ingredient</v-btn>
          </v-list-item-title>
        </v-list-item>
      </v-list-item-group>
    </v-list>
    <edit-ingredient v-model="editIngredientModel" @cancel="cancel" @input="inputted"></edit-ingredient>

  </v-form>
</template>

<script>
import EditIngredient from "@/components/EditIngredient";

export default {
  name: "Editor",
  components: {EditIngredient},
  props: ['value'],
  computed: {
    model: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('input', value)
      }
    }
  },
  data() {
    return {
      // ingredient: {
      //   quantity: 0,
      //   unit: "",
      //   name: ""
      // },
      editIngredientModel: {
        ingredient: {
          quantity: "",
          unit: "",
          name: ""
        },
        active: false,
        position: -1
      }
    }
  },
  watch: {
    'editIngredientModel.active': {
      deep: true,
      handler: function (val, oldVal) {
        console.log("watch edit ingredient model")
        console.log(JSON.stringify(oldVal))
        console.log(JSON.stringify(val))
        if (val && !oldVal) {
          console.log("Dialog aktivieren")
        } else {
          console.log("Dialog ausschalten")
        }
      }
    }
  },
  methods: {
    inputted(val) {
      console.log("Editor inputted")
      console.log(JSON.stringify(val))
      if (val.position !== -1 ) {
        this.model.ingredients[val.position] = val.ingredient
      } else {
        this.model.ingredients.push(val.ingredient)
      }
    },
    cancel() {
      console.log("Editor cancel")
    },
    editIngredient(index) {
      this.editIngredientModel = {
        ingredient: {...this.$props.value.ingredients[index]},
        active: true,
        position: index
      }
    },
    addIngredient() {
      console.log("add ingredient")
      this.editIngredientModel = {
        ingredient: {
          quantity: "",
          unit: "",
          name: ""
        },
        active: true,
        position: -1,
      }
    },
    deleteIngredient(pos) {
      console.log("delete ingredient " + pos)
      this.$props.value.ingredients.splice(pos, 1)
    }
  }
}

</script>

<style scoped>

</style>