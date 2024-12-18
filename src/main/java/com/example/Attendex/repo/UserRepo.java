package com.example.Attendex.repo;

import com.example.Attendex.model.CourseEntity;
import com.example.Attendex.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByUsername(String username);  // Find user by username
    long countByRole(String role); // Count users by role
    
    @Query("SELECT DISTINCT u FROM UserEntity u JOIN u.enrolledCourses ec WHERE ec = :course")
    List<UserEntity> findStudentsByCourse(@Param("course") CourseEntity course);
}
