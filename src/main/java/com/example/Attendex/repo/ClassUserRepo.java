package com.example.Attendex.repo;

import com.example.Attendex.model.ClassEntity;
import com.example.Attendex.model.ClassRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassUserRepo extends JpaRepository<ClassRegistration, Long> {

    List<ClassRegistration> findByStudentUsername(String username); // Custom query for student username

    List<ClassRegistration> findByClassEntity(ClassEntity classEntity); // Custom query for class entity

    // Custom query to get the list of usernames of students registered for a specific class
    @Query("SELECT cr.student.username FROM ClassRegistration cr WHERE cr.classEntity = :classEntity")
    List<String> findStudentUsernamesByClassEntity(ClassEntity classEntity);
}
