package com.example;

import com.google.common.base.Strings;
import org.hibernate.Session;

import javax.inject.Inject;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("/phonebook")
public class PhoneBookResource {
    @Inject
    EntityManager em;

    public static List<Contact> contacts = new ArrayList<>();

    //get request at http://localhost:8080/phonebook/
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoneBook(@QueryParam("page")Long page,@QueryParam("limit")Long limit) {
        final Contact[] cont = new Contact[1];
        contacts.clear();
        long n=(page-1)*limit;
        List<Object[]>  results =em.createNativeQuery("SELECT a.id, a.name, a.phonenumber FROM contact a WHERE a.id >"+n+" ORDER BY a.id ASC LIMIT "+limit+";").getResultList();
        results.stream().forEach((record) -> {
            Long id = ((Integer) record[0]).longValue();
            String name = (String) record[1];
            String phonenumber = (String) record[2];
            cont[0] = new Contact(id.intValue(),name,phonenumber);
            contacts.add(cont[0]);


        });
        cont[0] = null;
        return Response.ok(contacts).build();
    }


    //get request at http://localhost:8080/phonebook/sn
    @GET
    @Path("sn/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getContactFromNamePhoneBook(
            @PathParam("name") String name) {
        final Contact[] cont = new Contact[1];
        contacts.clear();
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(name)? null : "SELECT * FROM contact WHERE name=:name" ;
        if(!Strings.isNullOrEmpty(name)) {
            properties.put("name", name);
            List<Object[]> results = em.unwrap(Session.class).createNativeQuery(Query).setProperties(properties).getResultList();
            results.forEach((record) -> {
                Long id = ((Integer) record[0]).longValue();
                String onoma = (String) record[1];
                String phonenumber = (String) record[2];
                cont[0] = new Contact(id.intValue(), onoma, phonenumber);
                contacts.add(cont[0]);
            });
            cont[0] = null;
            return Response.ok(contacts).build();
        }else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }


    }

    //get request at http://localhost:8080/phonebook/sp
    @GET
    @Path("sp/{phonenumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getContactFromNumberPhoneBook(
            @PathParam("phonenumber") String phonenumber) {
        final Contact[] cont = new Contact[1];
        contacts.clear();
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(phonenumber)? null : "SELECT * FROM contact WHERE phonenumber=:phonenumber" ;
        if(!Strings.isNullOrEmpty(phonenumber)) {
            properties.put("phonenumber", phonenumber);
            List<Object[]> results = em.unwrap(Session.class).createNativeQuery(Query).setProperties(properties).getResultList();
            results.forEach((record) -> {
                Long id = ((Integer) record[0]).longValue();
                String onoma = (String) record[1];
                String thl = (String) record[2];
                cont[0] = new Contact(id.intValue(), onoma, thl);
                contacts.add(cont[0]);
            });
            cont[0] = null;
            return Response.ok(contacts).build();
        }else{
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }





    @GET
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/size")
    public BigInteger getCount(){
        Query results =em.createNativeQuery("SELECT count(a.id) FROM contact a");
        return (BigInteger) results.getSingleResult();
    }


    @GET
    @Transactional
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/searchsize")
    public BigInteger getCountFromName(@QueryParam("name")String name,@QueryParam("phonenumber")String phonenumber){
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(name)?"SELECT count(*) FROM (SELECT * FROM contact WHERE phonenumber=:phonenumber) as a":
                Strings.isNullOrEmpty(phonenumber)?"SELECT count(*) FROM (SELECT * FROM contact WHERE name=:name) as a":
                        "SELECT count(*) FROM (SELECT * FROM contact WHERE name=:name AND phonenumber=:phonenumber) as a";

        if (Strings.isNullOrEmpty(name)){
            properties.put("phonenumber",phonenumber);
        }else if(Strings.isNullOrEmpty(phonenumber)){
            properties.put("name",name);
        }else{
            properties.put("name",name);
            properties.put("phonenumber",phonenumber);
        }

        Query qu = em.unwrap(Session.class).createNativeQuery(Query).setProperties(properties);


        return (BigInteger) qu.getSingleResult();
    }

    //post request with JSON body (e.g.)
    //{
    //	"id": 2,
    //  "name": Tzonis,
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
        if ((newContact.getName()!=null) && (newContact.getPhonenumber()!=null) && (!newContact.getName().equals("")) && (!newContact.getPhonenumber().equals(""))) {
            try {
                em.persist(newContact);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            Instant instant2 = Instant.now();

            Duration time = Duration.between(now, instant2);
            System.out.println("Xreiastika :" + time + " gia to JPA");

            //JDBC
            //addContact_Jdbc(newContact);
            //Arraylist
            contacts.add(newContact);

            return Response.ok(contacts).build();
        }else{
            throw new BadRequestException();
        }
    }

    //put request
    //at http://localhost:8080/phonebook/2/gg/6947650188
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
            em.merge(con);
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