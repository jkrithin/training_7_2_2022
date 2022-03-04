package com.example;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
@SecondaryTable(name = "roles")
public class AppUser extends PanacheEntityBase {

    private static final Logger logger = LoggerFactory.getLogger(AppUser.class);
    @Id
    @Column(name = "id" , unique = true)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "username", nullable = false)
    private String username;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(table = "roles", name = "role")
    private String role;

    public AppUser() {
    }

    public AppUser(Long id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public Boolean areEqual(AppUser newUser) {

        logger.warn("name {} username {} ", this.getName(),this.getUsername());
        logger.warn("name {} username {} ", newUser.getName(),newUser.getUsername());
        return ((Objects.equals(this.getName(), newUser.getName()) ||
                Objects.equals(this.getName(), newUser.getUsername()) ||
                Objects.equals(this.getUsername(), newUser.getUsername())));
    }

}
