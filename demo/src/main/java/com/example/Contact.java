package com.example;

import com.sun.jdi.PathSearchingVirtualMachine;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="contact")
public class Contact extends PanacheEntityBase {
    @Id
    @Column(name = "contactid" , unique = true)
    private int id;
    @Column(name = "phonenumber", nullable = false)
    private String phonenumber;


    public Contact(Integer id, String phonenumber) {
        this.id = id;
        this.phonenumber = phonenumber;
    }

    public Contact() {

    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public Integer getId() {
        return id;
    }

    public String getPhonenumber() {
        return phonenumber;
    }
}
