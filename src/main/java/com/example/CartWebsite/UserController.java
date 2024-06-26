package com.example.CartWebsite;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid User user, BindingResult result, @RequestParam String confirmPassword, Model model) {
        if (result.hasErrors()) {
            return "signup";
        }
        if (userService.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "An account with this email already exists.");
            return "signup";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }
        userService.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String redirectUrl, Model model) {
        model.addAttribute("redirectUrl", redirectUrl);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        @RequestParam(required = false) String redirectUrl, HttpSession session, Model model) {
        User user = userService.findByEmail(email);
        Admin admin = adminService.findByEmail(email);

        if (user != null && password.equals(user.getPassword())) {
            session.setAttribute("user", user);
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/dashboard");
        } else if (admin != null && password.equals(admin.getPassword())) {
            session.setAttribute("admin", admin);
            return "redirect:/admin/home";
        } else {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", user);
        return "dashboard";
    }
}
