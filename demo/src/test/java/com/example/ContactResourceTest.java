package com.example;

import io.agroal.api.AgroalDataSource;
import java.time.*;
import io.quarkus.test.junit.QuarkusTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;



import static com.example.PhoneBookResource.contacts;
import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ContactResourceTest {
    @Inject
    EntityManager em;
    @Inject
    AgroalDataSource ds;

    private int BATCH_SIZE = 1000;


    @Transactional
    @BeforeEach
    public void cleanup() {
        contacts.clear();
        em.createNativeQuery("TRUNCATE contact").executeUpdate();
    }

    @Test
    public void testPhonebookGet() {
        given()
                .when().get("/phonebook")
                .then()
                .statusCode(200)
                .body(is("[]"));
    }


    @AfterEach
    @Transactional
    public void cleanupA() {
        contacts.clear();
        em.createNativeQuery("TRUNCATE contact").executeUpdate();
    }

    @Test
    public void testPhonebookSize() {
        given()
                .when().get("/phonebook/size")
                .then()
                .statusCode(200)
                .body(is("0"));
    }

    @Test
    public void testPhonebookInsert() {
        given()
                .when().body("{\"id\": 2,\"name\": \"foufout\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200)
                .body(is("[{\"id\":2,\"name\":\"foufout\",\"phonenumber\":\"6947650192\"}]"));
    }

    @Test
    public void testPhonebookReplace() {

        given()
                .when().body("{\"id\": 2,\"name\": \"gg\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200);
        given()
                .when().body("").contentType(APPLICATION_JSON).put("/phonebook/2/gg/6947650188")
                .then()
                .statusCode(200)
                .body(is("[{\"id\":2,\"name\":\"gg\",\"phonenumber\":\"6947650188\"}]"));

    }


    @Test
    public void testPhonebookRemove() {
        given()
                .when().body("{\"id\": 2,\"name\": \"gg\",\"phonenumber\": \"6947650192\"}").contentType(APPLICATION_JSON).post("/phonebook")
                .then()
                .statusCode(200);
        given()
                .when().body("").contentType(APPLICATION_JSON).delete("/phonebook/2/")
                .then()
                .statusCode(200)
                .body(is("[]"));

    }

    @Test
    public void addContact_Jdbc() {
        Instant now = null;
        Contact newContact = new Contact(5, "Foufoutos", "690000000");
        //creating Calendar instance

        Statement stmt = null;
        String query = "INSERT INTO contact(id,name, phonenumber) " +
                "VALUES (?,?,?);";

        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
            now = Instant.now();

            pstmt.setInt(1, newContact.getId());
            pstmt.setString(2, newContact.getPhonenumber());

        } catch (Exception e) {
            e.printStackTrace();
        }


        Instant instant2 = Instant.now();
        Duration time = Duration.between(now, instant2);
        System.out.println("Xreiastika :" + time + " gia to JDBC");

    }

    @Transactional
    @Test
    public void insertBatchJPA() {

        Instant now = Instant.now();
        for (int i = 0; i < 10000; i++) {

            Contact con = new Contact(i, "egw", "690000000");
            em.persist(con);
        }
        Instant instant2 = Instant.now();
        Duration time = Duration.between(now, instant2);
        System.out.println("Xreiastika :" + time + " gia to batch 10000 JPA");
    }

    @Test
    public void addBatchContacts_Jdbc() {

        Instant now = null;
        Statement stmt = null;
        String query = "INSERT INTO contact VALUES (?,?,?);";
        try (Connection con = ds.getConnection(); PreparedStatement pstmt = con.prepareStatement(query)) {
            con.setAutoCommit(false);
            now = Instant.now();
            for (int i = 1; i <= 10000; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "lolll");
                pstmt.setString(3, "690000");

                pstmt.addBatch();
            }
            int[] result = pstmt.executeBatch();
            //System.out.println("The number of rows inserted: "+ result.length);
            con.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instant instant2 = Instant.now();
        Duration time = Duration.between(now, instant2);
        System.out.println("Xreiastika :" + time + " gia to JDBC");

    }


}





/*
    public void insertBatchFruitsJPA() {
        Instant now = Instant.now();
        for (int i = 0; i < 10000; i++) {

            Fruit con = new Fruit("Apple");
            em.persist(con);
        }
        Instant instant2 = Instant.now();
        Duration time = Duration.between(now, instant2);
        System.out.println("Xreiastika :"+time+" gia to batch 10000 JPA me auto-generated");
    }


    public void addBatchFruits_Jdbc(){

        Instant now = null;


        String query = "INSERT INTO fruit VALUES (DEFAULT,?);";
        try{
            Connection con= DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/jkrithin","jkrithin","jkrithin");

            PreparedStatement pstmt = con.prepareStatement(query);
            con.setAutoCommit(false);

            now = Instant.now();
            for(int i=1; i<= 10000;i++){

                pstmt.setString(1,"Apple");

                pstmt.addBatch();
            }
            int[] result = pstmt.executeBatch();


            //System.out.println("The number of rows inserted: "+ result.length);
            con.commit();

        }catch(Exception e) {
            e.printStackTrace();
        }
        Instant instant2 = Instant.now();
        Duration time = Duration.between(now, instant2);


        System.out.println("Xreiastika :"+time+" gia to JDBC 10000 me auto-generated");

    }

*/


