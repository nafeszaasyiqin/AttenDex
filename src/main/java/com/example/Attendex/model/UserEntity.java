package com.example.Attendex.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.List;

import jakarta.persistence.Column;

@Entity
public class UserEntity {

    @Id
    private String username;

    @Column(nullable = false) // Ensure password is not null in the database
    private String password;

    private String role;

    // Default constructor
    public UserEntity() {}

    // Constructor with parameters (optional)
    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @ManyToMany
    @JoinTable(
        name = "course_students", // Join table name
        joinColumns = @JoinColumn(name = "student_username", referencedColumnName = "username"), // Column in join table pointing to UserEntity
        inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id") // Column in join table pointing to CourseEntity
    )
    private List<CourseEntity> enrolledCourses;

}
