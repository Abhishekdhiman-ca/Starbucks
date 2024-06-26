package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Object user = session.getAttribute("user");
        Object admin = session.getAttribute("admin");

        if (user != null) {
            model.addAttribute("isAdmin", false);
        } else if (admin != null) {
            model.addAttribute("isAdmin", true);
        } else {
            model.addAttribute("isAdmin", false);
        }

        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String category, Model model) {
        List<Product> products;
        if (category != null) {
            products = productRepository.findByCategory(category);
        } else {
            products = productRepository.findAll();
        }
        model.addAttribute("products", products);
        return "products";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/submit-contact")
    public String submitContact(@RequestParam String name, @RequestParam String email, @RequestParam String message, Model model) {
        // Handle form submission logic here
        model.addAttribute("message", "Thank you for contacting us, " + name + ". We will get back to you soon.");
        return "contact";
    }
}
