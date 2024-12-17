package com.example.Attendex.repo;

import com.example.Attendex.model.ClassSessionEntity;
import com.example.Attendex.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClassSessionRepo extends JpaRepository<ClassSessionEntity, Long> {
    List<ClassSessionEntity> findByCourse(CourseEntity course);
    List<ClassSessionEntity> findByCourseAndSessionDateTimeBetween(CourseEntity course, LocalDateTime start, LocalDateTime end);
    Optional<ClassSessionEntity> findByClassCodeAndActive(String classCode, boolean active);
    Long countByCourse(CourseEntity course);
}