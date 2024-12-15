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

    // Display registration form
    @GetMapping("/userRegis")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserEntity()); // Add a new UserEntity object to the model
        return "userRegis"; // Points to the userRegis.html template
    }

    // Handle user registration
    @PostMapping("/admin/register")
    public String registerUser(UserEntity user, RedirectAttributes redirectAttributes) {
        userRepo.save(user); // Save the new user in the database
        redirectAttributes.addFlashAttribute("message", "User successfully registered!"); // Flash success message
        return "redirect:/userRegis"; // Redirect back to the register page
    }

    // Display update form
    @GetMapping("/admin/update")
    public String showUpdateForm() {
        return "admin-update";
    }

    @GetMapping("/admin/view")
    public String viewUsers(Model model) {
        // Fetch all users from the database
        model.addAttribute("users", userRepo.findAll()); // Add all users to the model
        return "admin-view"; // Redirect to the view-users.html template
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


    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("studentCount", 10);  // Example dynamic data
        model.addAttribute("lecturerCount", 2);
        return "admin-dashboard";
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
