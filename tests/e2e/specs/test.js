// https://docs.cypress.io/api/table-of-contents

describe('Visit ', () => {
  it('Visits the app root url', () => {
    cy.visit('localhost:8081/phonebook')
  })
})


const user = {username: "jkrithin1" + "@cytech.gr", password: "123qwe"};
const userasuser = {username: "jkrithin2" + "@cytech.gr", password: "123qwe"};
const contact = {name: "jkrithin1" + "@dev+test", phonenumber: "6900000000"};
const editedcontact = {name: "jkr" + "@dev+test", phonenumber: "6900000001"};
describe('User login', () => {
  it('Visits Login page', () => {
    //make sure local storage is clear
    localStorage.clear();
    cy.visit('localhost:8081/login');
    //we check login errors.
    cy.get('button').should('exist');
    cy.get('window:alert').should('not.exist');
    cy.get('button').click({force:true});
    cy.on('window:alert', (str) => {
      expect(str).to.equal(`You could not be logged in. Please try again !`)
    })
    cy.get('input#input-17').should('exist');
    cy.get('input#password').should('exist');
  })

  it ('Submits Login form', () => {
    cy.get('input#input-17').type(user.username);
    cy.get('input#password').type(user.password);
    cy.get('button').click({force:true});
    cy.get('window:alert').should('not.exist');
    cy.get('input#input-17').should('not.exist');
    cy.get('input#password').should('not.exist');
    cy.get('input#password').should('not.exist');
    cy.get('p').should('exist');
    cy.get('input#input-28').should('exist');
    cy.get('input#input-31').should('exist');
    cy.get('button#search').should('exist');
    cy.get('button#add').should('exist');
    cy.get('#contactsPerpage').should('exist');
  });

  describe('User login', () => {
    it('Adds a new Contact, edits and then removes it', () => {
      cy.get('input#input-28').type(contact.name);
      cy.get('input#input-31').type(contact.phonenumber);
      cy.get('button#add').click({force: true});
      cy.get('window:alert').should('not.exist');
      cy.get('button#search').click({force: true});
      cy.get('window:alert').should('not.exist');
      //edit contact
      cy.get('input#input-28').clear();
      cy.get('input#input-31').clear();
      cy.get('input#input-28').type(editedcontact.name);
      cy.get('input#input-31').type(editedcontact.phonenumber);
      cy.get('button#edit').click({force: true});
      cy.get('window:alert').should('not.exist');
      cy.get('button#search').click({force: true});
      cy.get('window:alert').should('not.exist');
      cy.get('button#delete').click({force: true});
      cy.get('window:alert').should('not.exist');
      cy.get('button#search').click({force: true});
      cy.get('window:alert').should('not.exist');
    });

    it('Contacts per Page', () => {
      cy.get('input#contactsPerpage').should('exist');
      cy.get('input#input-28').clear();
      cy.get('input#input-31').clear();
      cy.get('button#search').click({force: true});
      cy.get('#contactsPerpage').click({force: true}).should('exist');

    });
  })

})
describe('User login as USER', () => {
  it('Visits ABOUT page', () => {
    //make sure local storage is clear
    localStorage.clear();
    cy.visit('localhost:8081/login');
    cy.get('input#input-17').type(userasuser.username);
    cy.get('input#password').type(userasuser.password);
    cy.get('button').click({force:true});
    cy.get('window:alert').should('not.exist');
    cy.get('#about').contains('Please LOGIN as admin');
  })
})