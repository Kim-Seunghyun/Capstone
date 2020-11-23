package practice.capston.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "/home/home";
    }

    @GetMapping("/register")
    public String register() {
        return "/home/register";
    }

    @GetMapping("/login")
    public String login() {
        return "/home/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }

    @PostMapping("/login_proc")
    public String loginProc() {
        return "redirect:/";
    }
}
