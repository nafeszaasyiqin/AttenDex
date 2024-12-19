package com.example.Attendex.model;

import jakarta.persistence.*;

@Entity
@Table(name = "course_students")  // Ensure the table name is course_students
public class CourseStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id") // Foreign key for CourseEntity (course_id)
    private CourseEntity courseEntity;

    @ManyToOne
    @JoinColumn(name = "student_username") // Foreign key for UserEntity (student_username)
    private UserEntity student;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseEntity getCourseEntity() {
        return courseEntity;
    }

    public void setCourseEntity(CourseEntity courseEntity) {
        this.courseEntity = courseEntity;
    }

    public UserEntity getStudent() {
        return student;
    }

    public void setStudent(UserEntity student) {
        this.student = student;
    }

}
