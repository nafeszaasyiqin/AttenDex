package com.example.Attendex.repo;

import com.example.Attendex.model.CourseEntity;
import com.example.Attendex.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepo extends JpaRepository<CourseEntity, Long> {
    List<CourseEntity> findByLecturer(UserEntity lecturer);
    List<CourseEntity> findByStudents(UserEntity student);
}