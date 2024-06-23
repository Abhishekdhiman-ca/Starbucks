package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = cartItemRepository.findAll();
            session.setAttribute("cartItems", cartItems);
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartQty", cartItems.size());
        model.addAttribute("total", calculateTotal(cartItems));
        return "home";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam String item, @RequestParam String image,
                            @RequestParam double price, HttpSession session) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
            session.setAttribute("cartItems", cartItems);
        }

        boolean itemExists = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getName().equals(item)) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                cartItemRepository.save(cartItem);
                itemExists = true;
                break;
            }
        }

        if (!itemExists) {
            CartItem newCartItem = new CartItem(item, image, price);
            cartItemRepository.save(newCartItem);
            cartItems.add(newCartItem);
        }

        session.setAttribute("cartItems", cartItems);
        return "redirect:/";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long id, HttpSession session) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems != null) {
            cartItems.removeIf(cartItem -> cartItem.getId().equals(id));
            cartItemRepository.deleteById(id);
            session.setAttribute("cartItems", cartItems);
        }
        return "redirect:/cart";
    }

    @PostMapping("/purchase")
    public String purchase(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "redirect:/checkout";
        }

        double totalAmount = calculateTotal(cartItems);

        cartItemRepository.deleteAll(); // Remove all items from the cart
        session.setAttribute("cartItems", new ArrayList<>()); // Clear session cartItems

        // Set success message and total amount
        model.addAttribute("message", "Your purchase of $" + totalAmount + " is successful!");

        return "purchaseSuccess";
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartQty", cartItems.size());

        double totalAmount = calculateTotal(cartItems);
        DecimalFormat df = new DecimalFormat("#0.00");
        String formattedTotal = df.format(totalAmount);

        model.addAttribute("total", formattedTotal);

        return "cart";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartQty", cartItems.size());
        model.addAttribute("total", calculateTotal(cartItems));
        model.addAttribute("user", user);
        return "checkout";
    }

    private double calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalAmount();
        }
        return total;
    }
}