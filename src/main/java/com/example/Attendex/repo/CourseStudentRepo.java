package com.example.Attendex.repo;

import com.example.Attendex.model.CourseEntity;
import com.example.Attendex.model.CourseStudent;
import com.example.Attendex.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;



public interface CourseStudentRepo extends JpaRepository<CourseStudent, Long> {

    // Find all course-student registrations for a given student
    List<CourseStudent> findByStudent(UserEntity student);

    // Find all students registered for a given course
    List<CourseStudent> findByCourseEntity(CourseEntity courseEntity);

    // Check if a student is already registered for a specific course
    boolean existsByCourseEntityAndStudent(CourseEntity courseEntity, UserEntity student);

    // Custom query to fetch usernames of students registered for a specific course
    @Query("SELECT cs.student.username FROM CourseStudent cs WHERE cs.courseEntity = :courseEntity")
    List<String> findStudentUsernamesByCourseEntity(CourseEntity courseEntity);
}
