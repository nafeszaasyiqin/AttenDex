package com.example.Attendex.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AttendanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSessionEntity classSession;

    @ManyToOne
    @JoinColumn(name = "student_username", nullable = false)
    private UserEntity student;

    @Column(nullable = false)
    private boolean present;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClassSessionEntity getClassSession() {
        return classSession;
    }

    public void setClassSession(ClassSessionEntity classSession) {
        this.classSession = classSession;
    }

    public UserEntity getStudent() {
        return student;
    }

    public void setStudent(UserEntity student) {
        this.student = student;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}