package com.example.Attendex.repo;

import com.example.Attendex.model.Attendance;
import com.example.Attendex.model.ClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    List<Attendance> findByClassEntity(ClassEntity classEntity);
}
