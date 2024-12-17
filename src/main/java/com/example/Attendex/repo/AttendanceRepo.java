package com.example.Attendex.repo;

import com.example.Attendex.model.AttendanceEntity;
import com.example.Attendex.model.ClassSessionEntity;
import com.example.Attendex.model.CourseEntity;
import com.example.Attendex.model.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttendanceRepo extends JpaRepository<AttendanceEntity, Long> {
    List<AttendanceEntity> findByClassSession(ClassSessionEntity classSession);
    List<AttendanceEntity> findByStudent(UserEntity student);
    Long countByStudentAndClassSession_CourseAndPresent(
        UserEntity user, 
        CourseEntity course, 
        Boolean present
    );
    boolean existsByClassSessionAndStudent(ClassSessionEntity classSession, UserEntity student);
    List<AttendanceEntity> findByStudentOrderByTimestampDesc(UserEntity student);
}
