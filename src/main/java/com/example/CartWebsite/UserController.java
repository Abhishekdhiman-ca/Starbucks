package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid User user, BindingResult result, @RequestParam String confirmPassword, Model model, HttpSession session) {
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
        String token = jwtUtil.generateToken(user.getEmail());
        session.setAttribute("user", user);
        session.setAttribute("token", token);
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
            String token = jwtUtil.generateToken(user.getEmail());
            session.setAttribute("user", user);
            session.setAttribute("token", token);
            return "redirect:" + (redirectUrl != null ? redirectUrl : "/dashboard");
        } else if (admin != null && password.equals(admin.getPassword())) {
            String token = jwtUtil.generateToken(admin.getEmail());
            session.setAttribute("admin", admin);
            session.setAttribute("token", token);
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
        if (isSessionExpired(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/settings")
    public String settings(HttpSession session, Model model) {
        if (isSessionExpired(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        return "settings";
    }

    @PostMapping("/update-user-info")
    public String updateUserInfo(HttpSession session, @RequestParam String firstName, @RequestParam String lastName) {
        if (isSessionExpired(session)) {
            return "redirect:/login";
        }
        User user = (User) session.getAttribute("user");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
        session.setAttribute("user", user);
        return "redirect:/settings";
    }

    private boolean isSessionExpired(HttpSession session) {
        return session.getAttribute("user") == null;
    }
}
