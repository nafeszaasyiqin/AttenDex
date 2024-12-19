
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



    @GetMapping("/lecturer/register-class")
    public String Administrator() {
        return "lecturer/register-class"; // Maps to welcome.html
    }

}
