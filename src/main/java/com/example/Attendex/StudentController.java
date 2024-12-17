package com.example.Attendex;

import com.example.Attendex.model.*;
import com.example.Attendex.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private ClassSessionRepo classSessionRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            UserEntity student = userRepo.findByUsername(authentication.getName()).orElseThrow();
            List<CourseEntity> courses = courseRepo.findByStudents(student);
            
            System.out.println("Found " + courses.size() + " courses for student");
            
            model.addAttribute("student", student);
            model.addAttribute("courses", courses);
            return "student/dashboard";
        } catch (Exception e) {
            System.err.println("Error in student dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // @GetMapping("/profile")
    // public String viewProfile(Model model, Authentication authentication) {
    //     UserEntity student = userRepo.findByUsername(authentication.getName())
    //             .orElseThrow(() -> new RuntimeException("Student not found"));
        
    //     // Get attendance statistics
    //     List<AttendanceEntity> allAttendance = attendanceRepo.findByStudent(student);
    //     long totalClasses = allAttendance.size();
    //     long attendedClasses = allAttendance.stream()
    //             .filter(AttendanceEntity::isPresent)
    //             .count();
        
    //     double attendanceRate = totalClasses > 0 
    //         ? (double) attendedClasses / totalClasses * 100 
    //         : 0;

    //     model.addAttribute("student", student);
    //     model.addAttribute("totalClasses", totalClasses);
    //     model.addAttribute("attendedClasses", attendedClasses);
    //     model.addAttribute("attendanceRate", String.format("%.1f", attendanceRate));
        
    //     return "student/profile";
    // }

    // @PostMapping("/update-profile")
    // @ResponseBody
    // public ResponseEntity<?> updateProfile(
    //         @RequestParam(required = false) String currentPassword,
    //         @RequestParam(required = false) String newPassword,
    //         Authentication authentication) {
        
    //     UserEntity student = userRepo.findByUsername(authentication.getName())
    //             .orElseThrow(() -> new RuntimeException("Student not found"));
        
    //     // Verify current password if provided
    //     if (currentPassword != null && !currentPassword.equals(student.getPassword())) {
    //         return ResponseEntity.badRequest().body("Current password is incorrect");
    //     }
        
    //     // Update password if new one is provided
    //     if (newPassword != null && !newPassword.isEmpty()) {
    //         student.setPassword(newPassword);
    //         userRepo.save(student);
    //     }
        
    //     return ResponseEntity.ok("Profile updated successfully");
    // }

    @GetMapping("/api/attendance-stats")
    @ResponseBody
    public ResponseEntity<?> getAttendanceStats(Authentication authentication) {
        UserEntity student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<CourseEntity> courses = courseRepo.findByStudents(student);
        
        List<Object> stats = courses.stream().map(course -> {
            Long totalSessions = classSessionRepo.countByCourse(course);
            Long attendedSessions = attendanceRepo.countByStudentAndClassSession_CourseAndPresent(
                student, course, true);
            
            return Map.of(
                "courseName", course.getCourseName(),
                "totalSessions", totalSessions,
                "attendedSessions", attendedSessions,
                "attendanceRate", totalSessions > 0 
                    ? (double) attendedSessions / totalSessions * 100 
                    : 0
            );
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/mark-attendance")
    @ResponseBody
    public ResponseEntity<?> markAttendance(
            @RequestParam Long courseId,
            @RequestParam String code,
            Authentication authentication) {
                
        
        // Get current student
        UserEntity student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Find active session with the given code
        Optional<ClassSessionEntity> session = classSessionRepo.findByClassCodeAndActive(code, true);
        
        if (session.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid or expired code");
        }

        // Verify the code belongs to the correct course
        ClassSessionEntity classSession = session.get();
        if (!classSession.getCourse().getId().equals(courseId)) {
            return ResponseEntity.badRequest().body("Invalid code for this course");
        }

        // Check if attendance already marked
        boolean alreadyMarked = attendanceRepo.existsByClassSessionAndStudent(classSession, student);
        if (alreadyMarked) {
            return ResponseEntity.badRequest().body("Attendance already marked");
        }

        // Mark attendance
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setClassSession(classSession);
        attendance.setStudent(student);
        attendance.setPresent(true);
        attendance.setTimestamp(LocalDateTime.now());
        attendanceRepo.save(attendance);

        return ResponseEntity.ok("Attendance marked successfully");
    }

    @GetMapping("/attendance-history")
    public String attendanceHistory(Model model, Authentication authentication) {
        UserEntity student = userRepo.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        List<AttendanceEntity> attendanceHistory = attendanceRepo.findByStudentOrderByTimestampDesc(student);
        model.addAttribute("attendanceHistory", attendanceHistory);
        
        return "student/attendance-history";
    }
}