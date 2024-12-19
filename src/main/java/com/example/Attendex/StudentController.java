package com.example.Attendex;

// Core Spring imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;

// Spring Security imports
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// Java utility imports
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Java time imports
import java.time.LocalDateTime;

// Your model imports
import com.example.Attendex.model.UserEntity;
import com.example.Attendex.model.CourseEntity;
import com.example.Attendex.model.ClassSessionEntity;
import com.example.Attendex.model.AttendanceEntity;
import com.example.Attendex.model.CourseStudent;

// Your repository imports
import com.example.Attendex.repo.UserRepo;
import com.example.Attendex.repo.CourseRepo;
import com.example.Attendex.repo.ClassSessionRepo;
import com.example.Attendex.repo.AttendanceRepo;
import com.example.Attendex.repo.CourseStudentRepo;


@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private CourseStudentRepo courseStudentRepo;

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

    // Show the course registration page
    @GetMapping("/course-register")
    public String showAvailableCourses(Model model, Authentication authentication) {
        try {
            // Fetch the currently authenticated user
            UserEntity student = userRepo.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Fetch all available courses (no student filtering)
            List<CourseEntity> courses = courseRepo.findAll();

            // Debug output (optional)
            System.out.println("Found " + courses.size() + " courses");

            // Add the user and courses to the model
            model.addAttribute("student", student);
            model.addAttribute("courses", courses);

            // You can also pass additional attributes like username and role if needed
            model.addAttribute("username", authentication.getName());
            model.addAttribute("role", authentication.getAuthorities().toString());

            // Return the course registration page
            return "student/course-register";

        } catch (Exception e) {
            System.err.println("Error in course registration page: " + e.getMessage());
            e.printStackTrace();
            throw e;  // Rethrow the exception if necessary
        }
    }



    @PostMapping("/course-register")
    public String registerForCourse(@RequestParam("classId") Long classId,
                                    RedirectAttributes redirectAttributes) {
        // Fetch the course by ID
        CourseEntity selectedCourse = courseRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        // Get the currently authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the student (user)
        UserEntity student = userRepo.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the student is already registered for this course
        if (courseStudentRepo.existsByCourseEntityAndStudent(selectedCourse, student)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are already registered for this course.");
            redirectAttributes.addFlashAttribute("errorCourseId", classId); // Add course-specific ID
            return "redirect:/student/course-register";
        }

        // Create a new CourseStudent entry
        CourseStudent courseRegistration = new CourseStudent();
        courseRegistration.setCourseEntity(selectedCourse);
        courseRegistration.setStudent(student);

        // Save the course registration
        courseStudentRepo.save(courseRegistration);

        // Add success message and course-specific ID
        redirectAttributes.addFlashAttribute("successMessage", "You have successfully registered for " + selectedCourse.getCourseName());
        redirectAttributes.addFlashAttribute("successCourseId", classId); // Add course-specific ID
        return "redirect:/student/course-register";
    }




    // Redirect after successful registration







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

            // Get the current student
            UserEntity student = userRepo.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Find active session with the given code
            Optional<ClassSessionEntity> sessionOptional = classSessionRepo.findByClassCodeAndActive(code, true);

            if (sessionOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid or expired code");
            }

            // Verify the session's expiration
            ClassSessionEntity classSession = sessionOptional.get();
            if (LocalDateTime.now().isAfter(classSession.getCodeExpiryDateTime())) {
                return ResponseEntity.badRequest().body("This code has expired");
            }

            // Verify the code belongs to the correct course
            if (!classSession.getCourse().getId().equals(courseId)) {
                return ResponseEntity.badRequest().body("Invalid code for this course");
            }

            // Check if attendance is already marked
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
            // Total registered classes
            int totalClasses = courseStudentRepo.countRegistrationsByStudent(student);

            // Total attended classes
            int attendedClasses = attendanceHistory.size();

            // Calculate attendance rate
            double attendanceRate = (totalClasses > 0) ? ((double) attendedClasses / totalClasses) * 100 : 0.0;

            // Add attributes to the model
            model.addAttribute("attendanceHistory", attendanceHistory);
            model.addAttribute("totalClasses", totalClasses);
            model.addAttribute("attendedClasses", attendedClasses);
            model.addAttribute("attendanceRate", String.format("%.2f", attendanceRate));

            return "student/attendance-history";
        }
    }