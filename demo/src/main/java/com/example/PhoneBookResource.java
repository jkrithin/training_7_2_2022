package com.example;

import com.google.common.base.Strings;
import io.agroal.api.AgroalDataSource;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.security.RolesAllowed;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/phonebook")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhoneBookResource {
    private static final Logger logger = LoggerFactory.getLogger(PhoneBookResource.class);
    @Inject
    EntityManager em;

    //get request at http://localhost:8080/phonebook/
    @GET
    @RolesAllowed("admin")
    public List<Contact> getPhoneBook(@QueryParam("page")Long page,@QueryParam("limit")Long limit) {
        if ((page == null) || (page < 1) || (limit == null) || (limit < 1)) {
            logger.warn("Wrong page/limit params at getPhoneBook: {},{}",page,limit);
            throw new BadRequestException();
        }
        long n = (page - 1) * limit;
        try {
            return em.unwrap(Session.class)
                    .createNativeQuery("SELECT a.id, a.name, a.phonenumber FROM contact a " +
                            "WHERE a.id >:n ORDER BY a.id LIMIT :limit",Contact.class)
                    .setParameter("n", n).setParameter("limit", limit).getResultList();
        } catch (Exception e) {
            logger.warn("DB exception in getPhoneBook! Params: {},{}",page,limit);
            logger.warn("Exception: ",e);
            throw new InternalServerErrorException();
        }
    }

    //get request at http://localhost:8080/phonebook/sn
    @GET
    @Path("sn/{name}")
    @Transactional
    @RolesAllowed("admin")
    public List<Contact> getContactFromNamePhoneBook(@PathParam("name") String name,
                                                     @QueryParam("page")Long page,
                                                     @QueryParam("limit")Long limit) {
        String Query = Strings.isNullOrEmpty(name) ?
                null : "SELECT * FROM contact WHERE name=:name ORDER BY id LIMIT :limit";
        if ((page == null) || (page < 1) || (limit == null) || (limit < 1)) {
            logger.warn("Wrong page/limit params at search contact via name: {},{}",page,limit);
            throw new BadRequestException();
        }
        if (!Strings.isNullOrEmpty(name)) {
            try {
                return em.unwrap(Session.class).createNativeQuery(Query, Contact.class)
                        .setParameter("name", name).setParameter("limit", limit).getResultList();
            } catch (Exception e) {
                logger.warn("DB Exception at search Contact only via name with params:  number {}",name);
                throw new InternalServerErrorException();
            }
        }
        throw new BadRequestException();
    }

    //get request at http://localhost:8080/phonebook/sp
    @GET
    @Path("sp/{phonenumber}")
    @RolesAllowed("admin")
    @Transactional
    public List<Contact> getContactFromNumberPhoneBook(@PathParam("phonenumber") String phonenumber,
                                                       @QueryParam("page")Long page,
                                                       @QueryParam("limit")Long limit) {
        String Query = Strings.isNullOrEmpty(phonenumber) ?
                null : "SELECT * FROM contact WHERE phonenumber=:phonenumber ORDER BY id LIMIT :limit";
        if (!Strings.isNullOrEmpty(phonenumber)) {
            return em.unwrap(Session.class).createNativeQuery(Query,Contact.class)
                    .setParameter("phonenumber", phonenumber).setParameter("limit", limit).getResultList();
        } else {
            logger.warn("DB Exception at search Contact only via number with params:  number {}",phonenumber);
            throw new BadRequestException();
        }
    }

    //get request at http://localhost:8080/phonebook/sn
    @GET
    @Path("snp/{name}/{phonenumber}")
    @Transactional
    @RolesAllowed("admin")
    public List<Contact> getContactFromNameAndNumberPhoneBook(
            @PathParam("name") String name,
            @PathParam("phonenumber") String phonenumber,
            @QueryParam("page")Long page,
            @QueryParam("limit")Long limit) {
        String Query = Strings.isNullOrEmpty(name) ?
                null : "SELECT * FROM contact WHERE name=:name AND phonenumber=:phonenumber ORDER BY id LIMIT :limit";
        if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(phonenumber)) {
            return em.unwrap(Session.class).createNativeQuery(Query,Contact.class)
                    .setParameter("name", name).setParameter("phonenumber", phonenumber).setParameter("limit",limit)
                    .getResultList();
        } else {
            logger.warn("Exception at search Contact via name AND number with params: name {} number {}",
                    name,phonenumber);
            throw new BadRequestException();
        }
    }

    @GET
    @Transactional
    @Path("/size")
    @RolesAllowed("admin")
    public Integer getCount() {
        Query results = em.createNativeQuery("SELECT max(a.id) FROM contact a");
        return (Integer) results.getSingleResult();
    }

    @GET
    @Transactional
    @RolesAllowed("admin")
    @Path("/searchsize")
    public BigInteger getCountFromName(
            @QueryParam("name")String name,
            @QueryParam("phonenumber")String phonenumber) {
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(name) ?
                "SELECT count(*) FROM (SELECT * FROM contact WHERE phonenumber=:phonenumber) as a" :
                Strings.isNullOrEmpty(phonenumber) ?
                        "SELECT count(*) FROM (SELECT * FROM contact WHERE name=:name) as a" :
                        "SELECT count(*) FROM " +
                                "(SELECT * FROM contact WHERE name=:name AND phonenumber=:phonenumber) as a";

        if (Strings.isNullOrEmpty(name)) {
            properties.put("phonenumber",phonenumber);
        } else if (Strings.isNullOrEmpty(phonenumber)) {
            properties.put("name",name);
        } else {
            properties.put("name",name);
            properties.put("phonenumber",phonenumber);
        }
        Query qu = em.unwrap(Session.class).createNativeQuery(Query).setProperties(properties);
        return (BigInteger) qu.getSingleResult();
    }

    //post request with JSON body (e.g.)
    //
    //"id": 2,
    //"name": Tzonis,
    //"phonenumber": 6947660192
    //
    //at http://localhost:8080/phonebook/
    @POST
    @Transactional
    @RolesAllowed("admin")
    public Response addContact(Contact newContact) {
        //JPA
        Instant now = Instant.now();
        if ((newContact.getName() != null) && (newContact.getPhonenumber() != null) &&
                (!newContact.getName().equals("")) && (!newContact.getPhonenumber().equals(""))) {
            try {
                em.persist(newContact);
            } catch (Exception ex) {
                logger.warn("DB Exception at addContact with params: name {} ",newContact.getName());
                ex.printStackTrace();
            }
            return Response.ok().build();
        } else {
            logger.warn("Exception at addContact with params: name {} number {}",newContact.getName(),newContact.getPhonenumber());
            throw new BadRequestException();
        }
    }

    //put request
    //at http://localhost:8080/phonebook/2/gg/6947650188
    @PUT
    @Path("{id}/{name}/{phonenumber}")
    @Transactional
    @RolesAllowed("admin")
    public Response updateContact(
            @PathParam("id") Integer id,
            @PathParam("name") String name,
            @PathParam("phonenumber") String phonenumber
    ) {
        logger.warn("EDITING CONTACT {} {} {}",id,name,phonenumber);
        Contact con;
        try {
            //db
            con = em.find(Contact.class, id);
            con.setPhonenumber(phonenumber);
            con.setName(name);
            // Save the contact object
            em.merge(con);
            return Response.ok().build();
        } catch (Exception e) {
            logger.warn("Exception at updateContact with params: id {} name {} phonenumber{}",id,name,phonenumber);
            logger.warn("Exception:  ",e);
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    //delete request at
    //http://localhost:8080/phonebook/2
    @DELETE
    @Path("{id}")
    @RolesAllowed("admin")
    @Transactional
    public Response removeContact(@PathParam("id") Integer id) {
        Contact con;
        try {
            con = em.find(Contact.class, id);
            em.remove(con);
        } catch (Exception ex) {
            logger.warn("Exception at removeContact with params: id {} ",id);
            logger.warn("Exception:  ",ex);
            ex.printStackTrace();
        }
        return Response.ok().build();
    }

}