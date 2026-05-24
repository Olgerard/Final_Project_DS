package controller;

import org.springframework.stereotype.Controller;
import service.BrokerService;
import domain.Order;
import domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import controller.OrderRequest;

import java.util.List;

@Controller
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    /** Homepage */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /** Quick health check */
    @GetMapping("/testservice")
    public String testService(Model model) {
        model.addAttribute("name", brokerService.getStatus());
        return "greeting";
    }

    /**
     * Place a new order (REST endpoint called by the frontend).
     *
     * Example request body:
     * {
     *   "customerName":    "Alice",
     *   "deliveryAddress": "123 Main St",
     *   "paymentInfo":     "4111111111111111",
     *   "eventId":         1
     * }
     */
    @PostMapping("/orders")
    @ResponseBody
    public ResponseEntity<Order> placeOrder(@RequestBody OrderRequest request) {
        Order order = brokerService.placeOrder(
                request.getCustomerName(),
                request.getDeliveryAddress(),
                request.getPaymentInfo(),
                request.getEventId()
        );

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.status(409).body(order); // 409 Conflict = could not complete
        }
    }

    /**
     * Manager endpoint: first sends you to login and then list all orders.
     */
    @GetMapping("/manager")
    public String managerPage(Model model) {
        model.addAttribute("orders", brokerService.getAllOrders());
        return "manager";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
