package com.example.Attendex;

import com.example.Attendex.model.UserEntity;
import com.example.Attendex.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class adminController {

    @Autowired
    private UserRepo userRepo;

    // Display lecturer registration form
    @GetMapping("/regis-lecturer")
    public String FormregisLect(Model model) {
        model.addAttribute("user", new UserEntity()); // Add a new UserEntity object to the model
        return "regis-lecturer"; // Points to the regis-lecturer.html template
    }


    // Handle lecturer registration
    @PostMapping("/admin/register-lecturer")
    public String regisLect(UserEntity user, RedirectAttributes redirectAttributes) {
        user.setRole("ROLE_LECTURER"); // Set role as lecturer
        userRepo.save(user); // Save the new user in the database
        redirectAttributes.addFlashAttribute("message", "Lecturer successfully registered!"); // Flash success message
        return "redirect:/regis-lecturer"; // Redirect back to the lecturer registration page
    }

    // Display student registration form
    @GetMapping("/regis-student")
    public String FormregisStud(Model model) {
        model.addAttribute("user", new UserEntity()); // Add a new UserEntity object to the model
        return "regis-student"; // Points to the regis-student.html template
    }

    // Handle student registration
    @PostMapping("/admin/register-student")
    public String regisStud(UserEntity user, RedirectAttributes redirectAttributes) {
        user.setRole("ROLE_STUDENT"); // Set role as student
        userRepo.save(user); // Save the new user in the database
        redirectAttributes.addFlashAttribute("message", "Student successfully registered!"); // Flash success message
        return "redirect:/regis-student"; // Redirect back to the student registration page
    }


    // Display update form
    @GetMapping("/admin/update")
    public String showUpdateForm() {
        return "admin-update";
    }

    @GetMapping("/view-student")
    public String viewStud(Model model) {
        // Fetch all users from the database
        model.addAttribute("users", userRepo.findAll()); // Add all users to the model
        return "view-student"; // Redirect to the view-users.html template
    }

    @GetMapping("/view-lecturer")
    public String viewLect(Model model) {
        // Fetch all users from the database
        model.addAttribute("users", userRepo.findAll()); // Add all users to the model
        return "view-lecturer"; // Redirect to the view-users.html template
    }



    // Handle user update
    @PostMapping("/admin/update")
    public String updateUser(String username, String password, String role, RedirectAttributes redirectAttributes) {
        UserEntity user = userRepo.findByUsername(username).orElse(null);
        if (user != null) {
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            }
            user.setRole(role);
            userRepo.save(user); // Save the updated user
            redirectAttributes.addFlashAttribute("message", "User successfully updated!"); // Flash success message
        }
        return "redirect:/admin/update"; // Redirect to admin page or list of users
    }

    // Display delete form
    @GetMapping("/admin/delete")
    public String showDeleteForm() {
        return "admin-delete";
    }

    @PostMapping("/admin/delete")
    public String deleteUser(String username, RedirectAttributes redirectAttributes) {
        userRepo.deleteById(username); // Delete the user by username
        redirectAttributes.addFlashAttribute("message", "User successfully deleted!"); // Flash success message
        return "redirect:/admin/delete"; // Redirect to admin page or list of users
    }


    @GetMapping("/admin-dashboard")
    public String getDashboard(Model model) {
        long studentCount = userRepo.countByRole("ROLE_STUDENT");
        long lecturerCount = userRepo.countByRole("ROLE_LECTURER");

        System.out.println("Student Count: " + studentCount);  // Debugging log
        System.out.println("Lecturer Count: " + lecturerCount);  // Debugging log

        model.addAttribute("studentCount", studentCount);
        model.addAttribute("lecturerCount", lecturerCount);

        return "admin-dashboard";  // Should be rendered with the counts
    }




    @GetMapping("/admin/manage-lecturers")
    public String showManageLecturers() {
        return "manage-lecturers"; // Corresponds to manage-lecturers.html
    }

    @GetMapping("/admin/manage-students")
    public String showManageStudents() {
        return "manage-students"; // Refers to manage-students.html
    }






}
