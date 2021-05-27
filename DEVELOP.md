# Develop

## Frontend

- [Node Version Manager (nvm)](https://github.com/nvm-sh/nvm)
- [Node Package Manager (npm)](https://www.npmjs.com/)
    - [Documentation](https://docs.npmjs.com/)
        - [Configuring your local environment](https://docs.npmjs.com/getting-started/configuring-your-local-environment/)
- [Vue CLI](https://cli.vuejs.org/)
    - [Getting Started](https://cli.vuejs.org/guide/)
    - [Installation](https://cli.vuejs.org/guide/installation.html)
- [Vue.js](https://v3.vuejs.org/)

Installation des Node Version Managers:

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.38.0/install.sh | bash
```

Installation von Node und npm:

```bash
nvm install --lts
```

Installation von @vue/cli

```
npm install -g @vue/cli
```

Vue.js Projekt anlegen:

```bash
vue create hello-world
```

Vue 2 auswählen! Bootstrap Vue ist nicht kompatibel zu Vue 3:
[Vue 3 support #5196](https://github.com/bootstrap-vue/bootstrap-vue/issues/5196)

Bootstrap Vue Plugin hinzufügen

```bash
cd hello-world
vue add bootstrap-vue
```

Sie dazu auch [Getting Started](https://bootstrap-vue.org/docs#vue-cli-3-plugin)