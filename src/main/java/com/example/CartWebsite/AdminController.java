package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/admin/view-orders")
    public String viewOrders(HttpSession session, Model model, @RequestParam(required = false) String status) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        List<Order> orders;
        if (status == null) {
            orders = orderService.getAllOrders();
        } else {
            orders = orderService.getOrdersByStatus(status);
        }
        model.addAttribute("showOrders", true);
        model.addAttribute("orders", orders);
        model.addAttribute("status", status);
        return "admin-dashboard";
    }

    @GetMapping("/admin/products")
    public String showAllProducts(@RequestParam(required = false) String category, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }

        List<Product> products;
        if (category == null || category.isEmpty()) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByCategory(category);
        }

        model.addAttribute("products", products);
        model.addAttribute("selectedCategory", category);  // Add the selected category to the model
        return "admin-products";
    }


    @PostMapping("/admin/update-order-status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String status, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        orderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/view-orders";
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
    public String updateProductForm(@RequestParam Long id, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
        return "update-product";
    }

    @PostMapping("/admin/update-product")
    public String updateProduct(@RequestParam Long productId, @RequestParam String productName, @RequestParam double productPrice, @RequestParam MultipartFile productImage, @RequestParam String productCategory, @RequestParam String productDescription, HttpSession session, Model model) throws IOException {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            product.setName(productName);
            product.setPrice(productPrice);
            product.setCategory(productCategory);
            product.setDescription(productDescription);
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
    public String deleteProductForm(@RequestParam Long id, HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("admin");
        if (admin == null) {
            return "redirect:/login";
        }
        Product product = productRepository.findById(id).orElse(null);
        model.addAttribute("product", product);
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
