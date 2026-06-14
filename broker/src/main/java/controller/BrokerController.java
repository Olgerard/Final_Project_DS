package broker.controller;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import broker.service.BrokerService;
import broker.domain.Order;
import broker.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import broker.controller.OrderRequest;
import broker.service.SupplierClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private SupplierClient supplierClient;

    /** Homepage */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("events", brokerService.getAllEvents());
        return "index";
    }

    @GetMapping("/event/{eventId}")
    public String eventPage(@PathVariable int eventId, Model model) {
        model.addAttribute("eventId", eventId);
        model.addAttribute("tickets", supplierClient.getTickets(eventId));
        model.addAttribute("accommodations", supplierClient.getAccommodations(eventId));
        model.addAttribute("transports", supplierClient.getTransport(eventId));
        return "event";
    }

    @PostMapping("/cart")
    public String cart(@RequestParam int eventId,
                       @RequestParam int ticketId,
                       @RequestParam int accommodationId,
                       @RequestParam int transportId,
                       @RequestParam int quantity,
                       @RequestParam String customerName,
                       @RequestParam String deliveryAddress,
                       @RequestParam String paymentInfo,
                       Model model) {
        model.addAttribute("eventId", eventId);
        model.addAttribute("ticketId", ticketId);
        model.addAttribute("accommodationId", accommodationId);
        model.addAttribute("transportId", transportId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("customerName", customerName);
        model.addAttribute("deliveryAddress", deliveryAddress);
        model.addAttribute("paymentInfo", paymentInfo);

        List<Map> tickets = supplierClient.getTickets(eventId);
        List<Map> accommodations = supplierClient.getAccommodations(eventId);
        List<Map> transports = supplierClient.getTransport(eventId);

        tickets.stream().filter(t -> Integer.valueOf(ticketId).equals(t.get("id"))).findFirst()
                .ifPresent(t -> model.addAttribute("ticketName", t.get("eventName")));
        accommodations.stream().filter(a -> Integer.valueOf(accommodationId).equals(a.get("id"))).findFirst()
                .ifPresent(a -> model.addAttribute("accommodationName", a.get("name")));
        transports.stream().filter(tr -> Integer.valueOf(transportId).equals(tr.get("id"))).findFirst()
                .ifPresent(tr -> model.addAttribute("transportName", tr.get("type")));

        return "cart";
    }

    @PostMapping("/order")
    public String placeOrder(@RequestParam int eventId,
                             @RequestParam int ticketId,
                             @RequestParam int accommodationId,
                             @RequestParam int transportId,
                             @RequestParam int quantity,
                             @RequestParam String customerName,
                             @RequestParam String paymentInfo,
                             @RequestParam String deliveryAddress) {
        Order order = brokerService.placeOrder(customerName, deliveryAddress, paymentInfo, eventId, ticketId, accommodationId, transportId, quantity);
        return "redirect:/confirmation/" + order.getOrderId();
    }

    @GetMapping("/confirmation/{orderId}")
    public String confirmation(@PathVariable int orderId, Model model) {
        Optional<Order> order = brokerService.getOrder(orderId);
        model.addAttribute("order", order.isPresent() ? order.get() : null);
        return "confirmation";
    }

    @GetMapping("/manager")
    public String manager(Model model) {
        List<Order> orders = brokerService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("confirmedCount", orders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count());
        model.addAttribute("cancelledCount", orders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());
        return "manager";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
