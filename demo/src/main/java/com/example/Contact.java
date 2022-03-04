package com.example;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "contact")
public class Contact extends PanacheEntityBase {
    @Id
    @Column(name = "id" , unique = true)
    private int id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "phonenumber", nullable = false)
    private String phonenumber;

    public Contact(Integer id, String name, String phonenumber) {
        this.id = id;
        this.phonenumber = phonenumber;
        this.name = name;
    }

    public Contact() {
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }
}
