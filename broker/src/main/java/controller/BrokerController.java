package controller;

import domain.Order;
import domain.ReservationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import service.BrokerService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class BrokerController {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${supplier.accommodation.url}")
    private String accommodationUrl;

    @Value("${supplier.ticket.url}")
    private String ticketUrl;

    @Value("${supplier.transport.url}")
    private String transportUrl;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("events", List.of(
                Map.of("id", 1, "name", "Tomorrowland", "location", "Boom", "date", "18-20 Jul 2025"),
                Map.of("id", 2, "name", "Gentse Feesten", "location", "Gent", "date", "11-20 Jul 2025"),
                Map.of("id", 3, "name", "Rock Werchter", "location", "Werchter", "date", "3-6 Jul 2025")
        ));
        return "index";
    }

    @GetMapping("/event/{eventId}")
    public String eventPage(@PathVariable int eventId, Model model) {
        List<?> tickets = fetchList(ticketUrl + "/tickets/" + eventId);
        List<?> accommodations = fetchList(accommodationUrl + "/accommodations/" + eventId);
        List<?> transports = fetchList(transportUrl + "/transport/" + eventId);

        model.addAttribute("eventId", eventId);
        model.addAttribute("tickets", tickets);
        model.addAttribute("accommodations", accommodations);
        model.addAttribute("transports", transports);
        return "event";
    }

    @PostMapping("/cart")
    public String cart(@RequestParam int eventId,
                       @RequestParam int ticketId,
                       @RequestParam int accommodationId,
                       @RequestParam int transportId,
                       @RequestParam int quantity,
                       @RequestParam String customerName,
                       @RequestParam String paymentInfo,
                       Model model) {
        model.addAttribute("eventId", eventId);
        model.addAttribute("ticketId", ticketId);
        model.addAttribute("accommodationId", accommodationId);
        model.addAttribute("transportId", transportId);
        model.addAttribute("quantity", quantity);
        model.addAttribute("customerName", customerName);
        model.addAttribute("paymentInfo", paymentInfo);
        return "cart";
    }

    @PostMapping("/order")
    public String placeOrder(@RequestParam int eventId,
                             @RequestParam int ticketId,
                             @RequestParam int accommodationId,
                             @RequestParam int transportId,
                             @RequestParam int quantity,
                             @RequestParam String customerName,
                             @RequestParam String paymentInfo) {
        Order order = new Order();
        order.setEventId(eventId);
        order.setTicketId(ticketId);
        order.setAccommodationId(accommodationId);
        order.setTransportId(transportId);
        order.setQuantity(quantity);
        order.setCustomerName(customerName);
        order.setPaymentInfo(paymentInfo);

        Order result = brokerService.placeOrder(order);
        return "redirect:/confirmation/" + result.getOrderId();
    }

    @GetMapping("/confirmation/{orderId}")
    public String confirmation(@PathVariable int orderId, Model model) {
        Order order = brokerService.getOrder(orderId);
        model.addAttribute("order", order);
        return "confirmation";
    }

    @GetMapping("/manager")
    public String manager(Model model) {
        model.addAttribute("orders", brokerService.getAllOrders());
        return "manager";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    private List<?> fetchList(String url) {
        try {
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Map>>() {});
            return resp.getBody() != null ? resp.getBody() : Collections.emptyList();
        } catch (RestClientException e) {
            return Collections.emptyList();
        }
    }
}
