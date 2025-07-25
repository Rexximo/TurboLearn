package com.example.turbolearn;

public class User {
    public String name;
    public String email;

    public User() {
        // Constructor kosong diperlukan untuk Firestore
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}