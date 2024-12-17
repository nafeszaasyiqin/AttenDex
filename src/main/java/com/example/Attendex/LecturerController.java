package com.example.Attendex;

import com.example.Attendex.model.ClassEntity;
import com.example.Attendex.model.ClassRegistration;
import com.example.Attendex.model.UserEntity;
import com.example.Attendex.model.Attendance; // Assuming you create an Attendance model
import com.example.Attendex.repo.ClassRepo;
import com.example.Attendex.repo.ClassUserRepo;
import com.example.Attendex.repo.UserRepo;
import com.example.Attendex.repo.AttendanceRepo; // Assuming you create an AttendanceRepo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class LecturerController {

    @Autowired
    private ClassRepo classRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClassUserRepo classRegistrationRepo;  // Repo for ClassRegistration

    @Autowired
    private AttendanceRepo attendanceRepo;  // Repo for Attendance

    // Display create class form
    @GetMapping("/lecturer/manage-class")
    public String showManageClasses(Model model) {
        return "lecturer/manage-class";  // Return the manage-class.html template
    }

    // Handle class creation
    @PostMapping("/lecturer/create-class")
    public String createClass(@RequestParam("className") String className,
                              @RequestParam("classDate") String classDate,
                              @RequestParam("classTime") String classTime,
                              RedirectAttributes redirectAttributes) {
        String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Optional<UserEntity> lecturer = userRepo.findByUsername(username);

        // Create and save the class entity
        ClassEntity newClass = new ClassEntity();
        newClass.setClassName(className);
        newClass.setClassDate(classDate);
        newClass.setClassTime(classTime);
        newClass.setLecturer(lecturer.orElse(null));

        classRepo.save(newClass);

        redirectAttributes.addFlashAttribute("message", "Class successfully created!");
        return "redirect:/lecturer/manage-class";
    }

    // Display classes for lecturer
    @GetMapping("/lecturer/view-class")
    public String viewClasses(@RequestParam(value = "filterOption", required = false) String filterOption,
                              @RequestParam(value = "startDate", required = false) String startDate,
                              @RequestParam(value = "endDate", required = false) String endDate,
                              Model model) {
        String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        Optional<UserEntity> lecturer = userRepo.findByUsername(username);

        List<ClassEntity> classes;

        if ("yesterday".equalsIgnoreCase(filterOption)) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            classes = classRepo.findByClassDate(yesterday.toString());
        } else if ("today".equalsIgnoreCase(filterOption)) {
            LocalDate today = LocalDate.now();
            classes = classRepo.findByClassDate(today.toString());
        } else if ("tomorrow".equalsIgnoreCase(filterOption)) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            classes = classRepo.findByClassDate(tomorrow.toString());
        } else if (startDate != null && endDate != null) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            classes = classRepo.findByClassTimeBetween(start.toString(), end.toString());
        } else {
            classes = classRepo.findByLecturer(lecturer.orElse(null));
        }

        model.addAttribute("classes", classes);
        return "lecturer/view-class";
    }

    // Display attendance page for a specific class
    @GetMapping("/lecturer/manage-attendance")
    public String showAttendancePage(@RequestParam("classId") Long classId, Model model) {
        // Get the class entity and registered students for this class
        ClassEntity classEntity = classRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));

        List<ClassRegistration> classRegistrations = classRegistrationRepo.findByClassEntity(classEntity);
        model.addAttribute("classEntity", classEntity);
        model.addAttribute("classRegistrations", classRegistrations);
        return "/lecturer/manage-attendance";  // Template to display attendance
    }

    // Handle attendance marking
    @PostMapping("/lecturer/manage-attendance")
    public String markAttendance(@RequestParam("classId") Long classId,
                                 @RequestParam Map<String, String> attendanceData,
                                 RedirectAttributes redirectAttributes) {
        ClassEntity classEntity = classRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));

        // Loop through the attendance data (which contains student usernames)
        for (String username : attendanceData.keySet()) {
            if ("true".equals(attendanceData.get(username))) {
                UserEntity student = userRepo.findByUsername(username)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found"));

                // Create and save attendance record
                Attendance attendance = new Attendance();
                attendance.setClassEntity(classEntity);
                attendance.setStudent(student);
                attendance.setPresent(true);

                attendanceRepo.save(attendance);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Attendance successfully marked!");
        return "redirect:/lecturer/manage-attendance?classId=" + classId;
    }
}
