module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/no-babel',
  transformIgnorePatterns: [
    '<rootDir>/node_modules/(?!vuetify)'
  ],
  setupFilesAfterEnv: ["./tests/unit/jest.setup.js"],
  reporters: ["default", ['jest-junit', {
    outputDirectory: 'build/reports/tests/unit',
    classNameTemplate: '{classname}',
    titleTemplate: '{title}'
  }]],
}
