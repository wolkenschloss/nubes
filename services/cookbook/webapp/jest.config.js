module.exports = {
    preset: '@vue/cli-plugin-unit-jest/presets/no-babel',
    transformIgnorePatterns: [
        '<rootDir>/node_modules/(?!vuetify|url-template)'
    ],
    setupFilesAfterEnv: ["./tests/unit/jest.setup.js"],
    reporters: [
        'jest-standard-reporter',
        ['jest-junit', {
            outputDirectory: 'build/reports/tests/unit',
            classNameTemplate: '{classname}',
            titleTemplate: '{title}'
        }]],
}
