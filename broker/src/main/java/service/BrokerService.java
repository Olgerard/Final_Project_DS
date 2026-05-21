package service;

import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BrokerService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${supplier.accommodation.url}")
    private String accommodationUrl;

    @Value("${supplier.ticket.url}")
    private String ticketUrl;

    @Value("${supplier.transport.url}")
    private String transportUrl;

    public Order placeOrder(Order order) {
        Integer accReservationId = null;
        Integer ticketReservationId = null;
        Integer transportReservationId = null;

        try {
            Map<String, Integer> accBody = new HashMap<>();
            accBody.put("accommodationId", order.getAccommodationId());
            accBody.put("quantity", order.getQuantity());
            ResponseEntity<Map> accResp = restTemplate.postForEntity(
                    accommodationUrl + "/reservations", accBody, Map.class);
            if (accResp.getStatusCode() == HttpStatus.CREATED && accResp.getBody() != null) {
                accReservationId = (Integer) accResp.getBody().get("id");
            }
        } catch (RestClientException e) { }

        try {
            Map<String, Integer> ticketBody = new HashMap<>();
            ticketBody.put("ticketId", order.getTicketId());
            ticketBody.put("quantity", order.getQuantity());
            ResponseEntity<Map> ticketResp = restTemplate.postForEntity(
                    ticketUrl + "/reservations", ticketBody, Map.class);
            if (ticketResp.getStatusCode() == HttpStatus.CREATED && ticketResp.getBody() != null) {
                ticketReservationId = (Integer) ticketResp.getBody().get("id");
            }
        } catch (RestClientException e) { }

        try {
            Map<String, Integer> transportBody = new HashMap<>();
            transportBody.put("transportId", order.getTransportId());
            transportBody.put("quantity", order.getQuantity());
            ResponseEntity<Map> transportResp = restTemplate.postForEntity(
                    transportUrl + "/reservations", transportBody, Map.class);
            if (transportResp.getStatusCode() == HttpStatus.CREATED && transportResp.getBody() != null) {
                transportReservationId = (Integer) transportResp.getBody().get("id");
            }
        } catch (RestClientException e) { }

        boolean allSucceeded = accReservationId != null && ticketReservationId != null && transportReservationId != null;

        if (allSucceeded) {
confirmReservation(accommodationUrl, accReservationId);
            confirmReservation(ticketUrl, ticketReservationId);
            confirmReservation(transportUrl, transportReservationId);
            order.setStatus(ReservationStatus.CONFIRMED);
        } else {
            if (accReservationId != null) cancelReservation(accommodationUrl, accReservationId);
            if (ticketReservationId != null) cancelReservation(ticketUrl, ticketReservationId);
            if (transportReservationId != null) cancelReservation(transportUrl, transportReservationId);
            order.setStatus(ReservationStatus.CANCELLED);
        }

        if (accReservationId != null) {
            OrderItem item = new OrderItem();
            item.setSupplier(Supplier.ACCOMMODATION);
            item.setReservationId(accReservationId);
            item.setStatus(order.getStatus());
            order.getItems().add(item);
        }
        if (ticketReservationId != null) {
            OrderItem item = new OrderItem();
            item.setSupplier(Supplier.TICKET);
            item.setReservationId(ticketReservationId);
            item.setStatus(order.getStatus());
            order.getItems().add(item);
        }
        if (transportReservationId != null) {
            OrderItem item = new OrderItem();
            item.setSupplier(Supplier.TRANSPORT);
            item.setReservationId(transportReservationId);
            item.setStatus(order.getStatus());
            order.getItems().add(item);
        }

        return orderRepository.save(order);
    }

    private void confirmReservation(String baseUrl, int reservationId) {
        try {
            restTemplate.put(baseUrl + "/reservations/" + reservationId + "/confirm", null);
        } catch (RestClientException ignored) {}
    }

    private void cancelReservation(String baseUrl, int reservationId) {
        try {
            restTemplate.put(baseUrl + "/reservations/" + reservationId + "/cancel", null);
        } catch (RestClientException ignored) {}
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrder(int orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
}
