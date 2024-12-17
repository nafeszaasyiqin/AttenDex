package com.example.Attendex.repo;

import com.example.Attendex.model.ClassEntity;
import com.example.Attendex.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepo extends JpaRepository<ClassEntity, Long> {
    List<ClassEntity> findByClassDate(String classDate);
    List<ClassEntity> findByClassTimeBetween(String startTime, String endTime);
    List<ClassEntity> findByLecturer(UserEntity lecturer); // Add this method
}
