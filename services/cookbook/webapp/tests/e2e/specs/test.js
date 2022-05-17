// https://docs.cypress.io/api/introduction/api.html

describe('My First Test', () => {
  it('Visits the app root url', () => {
    cy.intercept({method: 'GET', url: '/cookbook/units/groups'}, [])
    cy.intercept('GET','/cookbook/recipe?from=*&to=*&q=', {})
    cy.visit('https://localhost:8181/cookbook/')
    cy.contains('[data-cy=app-title]', 'Cookbook')
  })
})
