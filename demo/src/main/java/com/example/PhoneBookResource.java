package com.example;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Path("/phonebook")
public class PhoneBookResource {
    @Inject
    EntityManager em;

    public static List<Contact> contacts = new ArrayList<>();

    //get request at http://localhost:8080/phonebook/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoneBook() {
        return Response.ok(contacts).build();
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/size")
    public Integer getCount(){
        return contacts.size();
    }

    //post request with JSON body (e.g.)
    //{
    //	"id": 2,
    //	"phonenumber": 6947660192
    //}
    //at http://localhost:8080/phonebook/
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addContact(Contact newContact){
        //JPA


        Instant now = Instant.now();
        try{
            em.persist(newContact);
        }catch (Exception ex){
            ex.printStackTrace();
       }
        Instant instant2 = Instant.now();

        Duration time = Duration.between(now, instant2);
        System.out.println("Xreiastika :"+time+" gia to JPA");

        //JDBC
        //addContact_Jdbc(newContact);
        //Arraylist
        contacts.add(newContact);

        return Response.ok(contacts).build();
    }

    //put request
    //at http://localhost:8080/phonebook/2/6947650188
    @PUT
    @Path("{id}/{name}/{phonenumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateContact(
            @PathParam("id") Integer id,
            @PathParam("name") String name,
            @PathParam("phonenumber") String phonenumber){
        Contact con;
        for (Contact contact : contacts) {
            if (contact.getId().equals(id)) {
                //table
                contact.setPhonenumber(phonenumber);
                contact.setName(name);

            }
        }
        try {
            //db
            con = em.find(Contact.class, id);
            con.setPhonenumber(phonenumber);
            con.setName(name);

            // Save the contact object
            em.persist(con);
            return Response.ok(contacts).build();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    //delete request at
    //http://localhost:8080/phonebook/2
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response removeContact(
            @PathParam("id") Integer id){
        Contact con;
        contacts.removeIf(contact -> contact.getId().equals(id));
        try {

            con = em.find(Contact.class, id);
            em.remove(con);

        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return Response.ok(contacts).build();
    }
    ///////JDBC

}