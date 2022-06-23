package com.olalekan.fashionstore.Models;

import java.util.ArrayList;

public class User {
    private String uid;
    private String name;
//    private ArrayList<Store> stores = new ArrayList<>();
//    private String date_of_birth= "";
    private String email;
    private String contact_number;
//    private String location = "";

    public User() {
    }

    public User(String uid, String name, String date_of_birth, String email, String contact_number, String location) {
        this.uid = uid;
        this.name = name;
//        this.date_of_birth = date_of_birth;
        this.email = email;
        this.contact_number = contact_number;
//        this.location = location;
    }


    public User(String uid, String name, String email, String contact_number) {
        this.name = name;
        this.email = email;
        this.contact_number = contact_number;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public ArrayList<Store> getStores() {
//        return stores;
//    }
//
//    public void setStores(ArrayList<Store> stores) {
//        this.stores = stores;
//    }

//    public String getDate_of_birth() {
//        return date_of_birth;
//    }
//
//    public void setDate_of_birth(String date_of_birth) {
//        this.date_of_birth = date_of_birth;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

//    public String getLocation() {
//        return location;
//    }
//
//    public void setLocation(String location) {
//        this.location = location;
//    }
}
