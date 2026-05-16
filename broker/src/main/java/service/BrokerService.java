package broker.service;

import broker.domain.Order;
import broker.domain.OrderItem;
import broker.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import broker.domain.OrderRepository;
import broker.service.SupplierClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements Two-Phase Commit (2PC) between the broker and 3 suppliers.
 *
 * Phase 1 (Reserve): Ask all suppliers to reserve. If ANY fails → cancel all.
 * Phase 2 (Confirm): If all phase 1 succeed → confirm all.
 */
@Service
public class BrokerService {

    @Autowired
    private SupplierClient supplierClient;

    @Autowired
    private broker.domain.OrderRepository orderRepository;

    public String getStatus() {
        return "Service works";
    }

    /**
     * Places a composite order using 2PC.
     *
     * @param customerName    name of the customer
     * @param deliveryAddress delivery address
     * @param paymentInfo     payment info (simulated)
     * @param eventId         the event id used to look up availability at suppliers
     * @return the completed Order with status CONFIRMED or CANCELLED
     */
    public Order placeOrder(String customerName, String deliveryAddress,
                            String paymentInfo, int eventId) {

        Order order = new Order();
        order.setCustomerName(customerName);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentInfo(paymentInfo);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        // ------------------------------------------------------------------
        // Phase 1: Reserve at all 3 suppliers
        // ------------------------------------------------------------------
        System.out.println("2PC Phase 1: reserving at all suppliers for eventId=" + eventId);

        int accReservationId       = supplierClient.reserveAccommodation(eventId);
        int ticketReservationId    = supplierClient.reserveTicket(eventId);
        int transportReservationId = supplierClient.reserveTransport(eventId);

        boolean phase1Success =
                accReservationId != -1 &&
                ticketReservationId != -1 &&
                transportReservationId != -1;

        if (!phase1Success) {
            // At least one supplier failed → rollback everything
            System.out.println("2PC Phase 1 FAILED — rolling back reservations");
            if (accReservationId != -1)       supplierClient.cancelAccommodation(accReservationId);
            if (ticketReservationId != -1)    supplierClient.cancelTicket(ticketReservationId);
            if (transportReservationId != -1) supplierClient.cancelTransport(transportReservationId);

            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return order;
        }

        // Record the reservation ids on the order
        order.getItems().add(new OrderItem("accommodation", accReservationId));
        order.getItems().add(new OrderItem("ticket",        ticketReservationId));
        order.getItems().add(new OrderItem("transport",     transportReservationId));
        orderRepository.save(order);

        // ------------------------------------------------------------------
        // Phase 2: Confirm at all 3 suppliers
        // ------------------------------------------------------------------
        System.out.println("2PC Phase 2: confirming all reservations");

        boolean accConfirmed       = supplierClient.confirmAccommodation(accReservationId);
        boolean ticketConfirmed    = supplierClient.confirmTicket(ticketReservationId);
        boolean transportConfirmed = supplierClient.confirmTransport(transportReservationId);

        if (accConfirmed && ticketConfirmed && transportConfirmed) {
            order.setStatus(OrderStatus.CONFIRMED);
            System.out.println("2PC complete: order " + order.getOrderId() + " CONFIRMED");
        } else {
            // Partial confirm failure — cancel what we can (best-effort rollback)
            System.out.println("2PC Phase 2 FAILED — cancelling reservations");
            supplierClient.cancelAccommodation(accReservationId);
            supplierClient.cancelTicket(ticketReservationId);
            supplierClient.cancelTransport(transportReservationId);
            order.setStatus(OrderStatus.CANCELLED);
        }
        orderRepository.save(order);
        return order;
    }

    /** Returns all orders (for manager view). */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
