package com.example;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class ContactResourceTest {
    @Inject
    EntityManager em;
    @Inject
    AgroalDataSource ds;
    @Inject
    AuthHandler auth;


    @Transactional
    @BeforeEach
    public void cleanup() {
        em.createNativeQuery("TRUNCATE contact").executeUpdate();
    }

    @Test
    public void testPhonebookGet() {
        String jwt = auth.generateJwt("jkrithin1","admin");
        Map headers = new HashMap<String,String>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).get("/phonebook?page=1&limit=5")
                .then()
                .statusCode(200);
    }


    @AfterEach
    @Transactional
    public void cleanupA() {
        em.createNativeQuery("TRUNCATE contact").executeUpdate();
    }

    //@Test
    @Transactional
    public void setUpEverything(){
        //for testing purpose only
        String query = "INSERT INTO contact VALUES (?,?,?);";
        try(
        Connection con = ds.getConnection();
        PreparedStatement pstmt = con.prepareStatement(query))
        {
            con.setAutoCommit(false);
            for (int i = 1; i < 1000000; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "foufoutos" + i);
                pstmt.setString(3, "09011"+i);
                pstmt.addBatch();

            }
            pstmt.executeBatch();
        } catch(Exception e){
            e.printStackTrace();
        }
        //users insert
    //        String query_user = "INSERT INTO users VALUES (?,?,?,?);";
    //        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query_user)) {
    //            con.setAutoCommit(false);
    //            for (int i = 1; i < 5; i++) {
    //                pstmt.setInt(1, i);
    //                pstmt.setString(2, "jkrithin"+i);
    //                pstmt.setString(3, "jkrithin"+i+"@cytech.gr");
    //                pstmt.setString(4, "123qwe");
    //                pstmt.addBatch();
    //            }
    //            con.commit();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        String query_role = "INSERT INTO roles VALUES (?,?);";
    //        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query_role)) {
    //            con.setAutoCommit(false);
    //            for (int i = 1; i < 5; i++) {
    //                pstmt.setInt(1, i);
    //                pstmt.setString(2, "admin");
    //                pstmt.addBatch();
    //            }
    //            con.commit();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    }

    @Test
    public void testJwt() {
        String jwt = auth.generateJwt("jkrithin1","admin");
        Map headers = new HashMap<String,String>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).get("/login/test-security-jwt").then()
                .body(is("jkrithin1:admin"));
    }


    @Test
    public void testPhonebookSize() {
        String jwt = auth.generateJwt("jkrithin1","admin");
        Map headers = new HashMap<String,String>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).get("/phonebook/size")
                .then()
                .statusCode(204);
    }

    @Test
    public void testPhonebookInsert() {
        String jwt = auth.generateJwt("jkrithin1","admin");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).body("{\"id\": 2,\"name\": \"foufout\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200);
    }

    @Test
    public void testPhonebookReplace() {

        String jwt = auth.generateJwt("jkrithin1","admin");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).body("{\"id\": 2,\"name\": \"gg\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200);
        given()
                .when().headers(headers).body("").contentType(APPLICATION_JSON).put("/phonebook/2/gg/6947650188")
                .then()
                .statusCode(200);

    }


    @Test
    public void testPhonebookRemove() {
        String jwt = auth.generateJwt("jkrithin1","admin");
        Map headers = new HashMap<String,String>();
        headers.put("Authorization","Bearer "+jwt);
        given()
                .when().headers(headers).body("{\"id\": 2,\"name\": \"gg\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200);
        given()
                .when().headers(headers).body("").contentType(APPLICATION_JSON).delete("/phonebook/2/")
                .then()
                .statusCode(200);

    }

    @Test
    public void addContact_Jdbc() {
        Instant now = null;
        Contact newContact = new Contact(5, "Foufoutos", "690000000");
        //creating Calendar instance
        String query = "INSERT INTO contact(id,name, phonenumber) " +
                "VALUES (?,?,?);";

        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
            now = Instant.now();

            pstmt.setInt(1, newContact.getId());
            pstmt.setString(2, newContact.getPhonenumber());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    @Test
    public void insertBatchJPA() {
        for (int i = 0; i < 10000; i++) {

            Contact con = new Contact(i, "egw", "690000000");
            em.persist(con);
        }
    }

    @Test
    public void addBatchContacts_Jdbc() {

        String query = "INSERT INTO contact VALUES (?,?,?);";
        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
            con.setAutoCommit(false);
            for (int i = 1; i <= 10000; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "lolll");
                pstmt.setString(3, "690000");

                pstmt.addBatch();
            }
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}





