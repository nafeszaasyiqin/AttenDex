
package com.example.Attendex;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WelcomeController {

    @GetMapping("/welcome")
    public String greeting() {
        return "welcome"; // Maps to welcome.html
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Maps to login.html
    }

    @GetMapping("/lecturer/lecturer")
    public String lecturerDashboard() {
        return "/lecturer/lecturer"; // Maps to lecturer.html
    }



    /*
    @GetMapping("/admin-dashboard")
    public String Administrator() {
        return "admin-dashboard"; // Maps to welcome.html
    } */

    @GetMapping("/manage-class")
    public String manageClasses() {
        return "lecturer/manage-class"; // Points to templates/lecturer/manage-class.html
    }
}
