package com.example.Attendex;

import com.example.Attendex.model.ClassEntity;
import com.example.Attendex.model.UserEntity;
import com.example.Attendex.model.ClassRegistration;
import com.example.Attendex.repo.ClassRepo;
import com.example.Attendex.repo.UserRepo;
import com.example.Attendex.repo.ClassUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Controller
public class ClassUserController {

    @Autowired
    private ClassRepo classRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ClassUserRepo classUserRepo;

    // Show the student home page after login
    @GetMapping("/student/student")
    public String studentHome() {
        return "/student/student";
    }

    // Show the class registration page
    @GetMapping("/student/class-register")
    public String showAvailableClasses(Model model) {
        // Fetch all classes
        List<ClassEntity> classes = classRepo.findAll();
        model.addAttribute("classes", classes);
        return "/student/class-register";
    }

    @PostMapping("/student/class-register")
    public String registerForClass(@RequestParam("classId") Long classId,
                                   RedirectAttributes redirectAttributes) {
        // Fetch the class by ID
        ClassEntity selectedClass = classRepo.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid class ID"));

        // Get the currently authenticated username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get username of logged-in user

        // Fetch the student (user)
        UserEntity student = userRepo.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create a new ClassRegistration entry
        ClassRegistration classRegistration = new ClassRegistration();
        classRegistration.setClassEntity(selectedClass);
        classRegistration.setStudent(student);

        // Save the class registration
        classUserRepo.save(classRegistration);

        // Add success message and redirect
        redirectAttributes.addFlashAttribute("message", "You have successfully registered for " + selectedClass.getClassName());
        return "redirect:/student/class-register"; // Redirect after successful registration
    }

    // Show the student's registered classes
    @GetMapping("/student/class-view")
    public String showRegisteredClasses(Model model) {
        // Get the logged-in student username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Fetch the student's registrations
        List<ClassRegistration> classRegistrations = classUserRepo.findByStudentUsername(username);

        // Add the registrations to the model
        model.addAttribute("classRegistrations", classRegistrations);

        // Add lecturer names to the model (using getLecturerName())
        for (ClassRegistration registration : classRegistrations) {
            String lecturerName = registration.getLecturerName(); // Get the lecturer's name
            model.addAttribute("lecturerName_" + registration.getId(), lecturerName);
        }

        return "/student/class-view"; // Path to your view
    }
}
