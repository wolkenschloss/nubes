module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/no-babel',
  setupFilesAfterEnv: ["./tests/unit/jest.setup.js"],
  reporters: ["default", ['jest-junit', {
    outputDirectory: 'build/results/tests/unit',
    classNameTemplate: '{classname}',
    titleTemplate: '{title}'
  }]],
}
