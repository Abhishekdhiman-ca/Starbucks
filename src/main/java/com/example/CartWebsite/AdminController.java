package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    private static final String UPLOAD_DIR = "src/main/resources/static/";

    @GetMapping("/admin/home")
    public String adminHome(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        return "home-admin";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("products", productRepository.findAll());
        return "admin-dashboard";
    }

    @PostMapping("/admin/update-order-status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/add-product")
    public String addProductForm(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        return "add-product";
    }

    @PostMapping("/admin/add-product")
    public String addProduct(@RequestParam String productName, @RequestParam double productPrice, @RequestParam MultipartFile productImage, @RequestParam String productCategory, HttpSession session, Model model) throws IOException {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }

        String imageName = productImage.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + imageName);
        Files.write(path, productImage.getBytes());

        Product product = new Product();
        product.setName(productName);
        product.setPrice(productPrice);
        product.setImage(imageName);
        product.setCategory(productCategory);
        productRepository.save(product);

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/update-product")
    public String updateProductForm(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "update-product";
    }

    @PostMapping("/admin/update-product")
    public String updateProduct(@RequestParam Long productId, @RequestParam String productName, @RequestParam double productPrice, @RequestParam MultipartFile productImage, @RequestParam String productCategory, HttpSession session, Model model) throws IOException {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setName(productName);
            product.setPrice(productPrice);
            product.setCategory(productCategory);
            if (!productImage.isEmpty()) {
                String imageName = productImage.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + imageName);
                Files.write(path, productImage.getBytes());
                product.setImage(imageName);
            }
            productRepository.save(product);
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/delete-product")
    public String deleteProductForm(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "delete-product";
    }

    @PostMapping("/admin/delete-product")
    public String deleteProduct(@RequestParam Long productId, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        productRepository.deleteById(productId);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/order-detail/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        Order order = orderService.getOrderById(id);
        if (order != null) {
            List<OrderDetail> orderDetails = orderService.getOrderDetailsByOrderId(order.getId());
            model.addAttribute("order", order);
            model.addAttribute("orderDetails", orderDetails);
            return "order-detail";
        } else {
            return "redirect:/admin/dashboard";
        }
    }

}
