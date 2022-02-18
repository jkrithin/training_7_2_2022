
describe('Simple tests', () => {
    it('Using query commands', () => {
        cy.visit('https://example.cypress.io/commands/querying');
        cy.get('button').should('contain', 'Button')
    });
});
