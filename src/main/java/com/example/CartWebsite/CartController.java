package com.example.CartWebsite;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

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

    private double calculateTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalAmount();
        }
        return total;
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

        Order order = new Order();
        order.setUserId(user.getId());
        order.setDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("Pending");
        order.setOrderTime(LocalDateTime.now());
        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            orderDetail.setItem(cartItem.getName());
            orderDetail.setImage(cartItem.getImage());
            orderDetail.setPrice(cartItem.getPrice());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setUserId(user.getId());
            orderDetailRepository.save(orderDetail);
        }

        cartItemRepository.deleteAll(); // Remove all items from the cart
        session.setAttribute("cartItems", new ArrayList<>()); // Clear session cartItems

        model.addAttribute("message", "Your purchase of $" + totalAmount + " is successful!");

        return "purchaseSuccess";
    }

    @GetMapping
    public String cart(HttpSession session, Model model) {
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartQty", cartItems.size());

        double totalAmount = calculateTotal(cartItems);
        model.addAttribute("total", totalAmount);

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

    @GetMapping("/my-orders")
    public String myOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            List<Order> orders = orderService.getOrdersByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("orders", orders);
            return "my-orders";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/order-detail/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Order order = orderService.getOrderById(id);
            if (order != null && order.getUserId().equals(user.getId())) {
                List<OrderDetail> orderDetails = orderService.getOrderDetailsByOrderId(order.getId());
                model.addAttribute("order", order);
                model.addAttribute("orderDetails", orderDetails);
                model.addAttribute("user", user);
                return "order-detail";
            } else {
                return "redirect:/cart/my-orders";
            }

        } else {
            return "redirect:/login";
        }
    }
}
