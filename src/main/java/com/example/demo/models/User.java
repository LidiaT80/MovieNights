package com.example.demo.models;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Entity(name = "user")
@Table(name = "user")
@DynamicUpdate
public class User {

    @Id
    @GeneratedValue
    private long id;
    @Column(name = "name")
    @NotNull
    private String name;
    @Column(name = "email")
    @Email
    private String email;

    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
