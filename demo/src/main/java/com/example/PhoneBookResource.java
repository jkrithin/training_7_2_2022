package com.example;

import com.google.common.base.Strings;
import io.agroal.api.AgroalDataSource;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger= LoggerFactory.getLogger(PhoneBookResource.class);
    @Inject
    EntityManager em;
    @Inject
    AgroalDataSource ds;



    //get request at http://localhost:8080/phonebook/
    @GET
    public List<Contact> getPhoneBook(@QueryParam("page")Long page, @QueryParam("limit")Long limit) {

        if((page==null)||(page<1)||(limit==null)||(limit<1)){
            logger.warn("Lathos Parametroi apo ton Xristi {},{}",page,limit);
            throw new BadRequestException();
        }
        long n=(page-1)*limit;
        try {
            return em.unwrap(Session.class)
                    .createNativeQuery("SELECT a.id, a.name, a.phonenumber FROM contact a WHERE a.id >:n ORDER BY a.id ASC LIMIT :limit", Contact.class)
                    .setParameter("n", n).setParameter("limit", limit).getResultList();
        }catch (Exception e){
            logger.warn("Backend/DB problem in getPhoneBook! Params: {},{}",page,limit);
            throw new InternalServerErrorException();
        }
    }


    //get request at http://localhost:8080/phonebook/sn
    @GET
    @Path("sn/{name}")
    @Transactional
    public List<Contact> getContactFromNamePhoneBook(
            @PathParam("name") String name,
            @QueryParam("page")Long page,
            @QueryParam("limit")Long limit
    ) {
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(name)? null : "SELECT * FROM contact WHERE name=:name ORDER BY id ASC LIMIT :limit";
        if((page==null)||(page<1)||(limit==null)||(limit<1)){
            logger.warn("Lathos Parametroi apo ton Xristi stin getContactFromNamePhoneBook: {},{}",page,limit);
            throw new BadRequestException();
        }
        if(!Strings.isNullOrEmpty(name)) {
            properties.put("name", name);
            properties.put("limit", limit.toString());
            try {

                return em.unwrap(Session.class).createNativeQuery(Query, Contact.class).setProperties(properties).getResultList();

            } catch (Exception e) {
                logger.warn("Backend/DB problem in getContactFromNamePhoneBook ! Params: {},{}", page, limit);
                throw new InternalServerErrorException();
            }
        }
        throw new BadRequestException();
    }

    //get request at http://localhost:8080/phonebook/sp
    @GET
    @Path("sp/{phonenumber}")
    @Transactional
    public List<Contact> getContactFromNumberPhoneBook(
            @PathParam("phonenumber") String phonenumber,
            @QueryParam("page")Long page,
            @QueryParam("limit")Long limit
    ) {
        long n=(page-1)*limit;
        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(phonenumber)? null : "SELECT * FROM contact WHERE phonenumber=:phonenumber ORDER BY id ASC LIMIT :limit";
        if(!Strings.isNullOrEmpty(phonenumber)) {
            properties.put("phonenumber", phonenumber);
            properties.put("limit", limit.toString());
            return em.unwrap(Session.class).createNativeQuery(Query,Contact.class).setProperties(properties).getResultList();
        }else{
            throw new BadRequestException();
        }
    }

    //get request at http://localhost:8080/phonebook/sn
    @GET
    @Path("snp/{name}/{phonenumber}")
    @Transactional
    public List<Contact> getContactFromNameAndNumberPhoneBook(
            @PathParam("name") String name,
            @PathParam("phonenumber") String phonenumber,
            @QueryParam("page")Long page,
            @QueryParam("limit")Long limit
    ) {

        Map<String,String> properties = new HashMap<>();
        String Query = Strings.isNullOrEmpty(name)? null : "SELECT * FROM contact WHERE name=:name AND phonenumber=:phonenumber ORDER BY id ASC LIMIT :limit";
        if(!Strings.isNullOrEmpty(name)&&!Strings.isNullOrEmpty(phonenumber)) {
            properties.put("name", name);
            properties.put("phonenumber", phonenumber);
            properties.put("limit", limit.toString());
            return em.unwrap(Session.class).createNativeQuery(Query,Contact.class).setProperties(properties).getResultList();
        }else{
            throw new BadRequestException();
        }
    }



    @GET
    @Transactional
    @Path("/size")
    public Integer getCount(){

        //for testing purpose only
//        Statement stmt = null;
//        String query = "INSERT INTO contact VALUES (?,?,?);";
//        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
//            con.setAutoCommit(false);
//            for (int i = 1; i < 1000000; i++) {
//                pstmt.setInt(1, i);
//                pstmt.setString(2, "foufoutos");
//                pstmt.setString(3, "09011");
//                pstmt.addBatch();
//            }
//            int[] result = pstmt.executeBatch();
//            //System.out.println("The number of rows inserted: "+ result.length);
//            con.commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        Query results =em.createNativeQuery("SELECT max(a.id) FROM contact a");
        return (Integer) results.getSingleResult();
    }


    @GET
    @Transactional
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

            return Response.ok().build();
        }else{
            throw new BadRequestException();
        }
    }

    //put request
    //at http://localhost:8080/phonebook/2/gg/6947650188
    @PUT
    @Path("{id}/{name}/{phonenumber}")
    @Transactional
    public Response updateContact(
            @PathParam("id") Integer id,
            @PathParam("name") String name,
            @PathParam("phonenumber") String phonenumber){
        Contact con;
        try {
            //db
            con = em.find(Contact.class, id);
            con.setPhonenumber(phonenumber);
            con.setName(name);
            // Save the contact object
            em.merge(con);
            return Response.ok().build();
        }catch (Exception e){
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    //delete request at
    //http://localhost:8080/phonebook/2
    @DELETE
    @Path("{id}")
    @Transactional
    public Response removeContact(
            @PathParam("id") Integer id){
        Contact con;
        try {
            con = em.find(Contact.class, id);
            em.remove(con);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Response.ok().build();
    }
    ///////JDBC

}