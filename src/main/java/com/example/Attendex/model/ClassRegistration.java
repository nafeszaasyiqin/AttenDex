package com.example.Attendex.model;

import jakarta.persistence.*;

@Entity
public class ClassRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_id") // Foreign key for ClassEntity
    private ClassEntity classEntity;

    @ManyToOne
    @JoinColumn(name = "student_username") // Foreign key for UserEntity (student)
    private UserEntity student;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClassEntity getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(ClassEntity classEntity) {
        this.classEntity = classEntity;
    }

    public UserEntity getStudent() {
        return student;
    }

    public void setStudent(UserEntity student) {
        this.student = student;
    }

    // Method to get the lecturer's name from the associated class
    public String getLecturerName() {
        if (classEntity != null && classEntity.getLecturer() != null) {
            return classEntity.getLecturer().getUsername(); // Or use getFullName() if available
        }
        return null;
    }
}
