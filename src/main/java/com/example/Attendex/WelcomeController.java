
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

    @GetMapping("/lecturer")
    public String lecturerDashboard() {
        return "lecturer"; // Maps to lecturer.html
    }

    @GetMapping("/student")
    public String studentDashboard() {
        return "student"; // Maps to student.html
    }

    @GetMapping("/admin")
    public String Administrator() {
        return "admin"; // Maps to welcome.html
    }
}
