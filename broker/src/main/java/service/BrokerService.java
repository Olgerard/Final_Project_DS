package broker.service;

import broker.domain.Order;
import broker.domain.OrderRepository;
import broker.domain.Event;
import broker.domain.EventRepository;
import broker.domain.OrderStatus;
import broker.domain.OrderItem;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import broker.service.SupplierClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private OrderRepository orderRepository;

    @Autowired
    private EventRepository eventRepository;

    //Recovering Pending requests after broker crash
    @PostConstruct
    public void recoverPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            boolean anyConfirmed = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderStatus.CONFIRMED);
            boolean anyConfirming = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderStatus.CONFIRMING);
            boolean anyCancelling = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderStatus.CANCELLING);
            if (anyConfirmed || anyConfirming) {
                // Broker crashed during confirmation and not all items are confirmed yet
                for (OrderItem item : order.getItems()) {
                    if (item.getStatus() == OrderStatus.PENDING || item.getStatus()==OrderStatus.CONFIRMING) {
                        if ("accommodation".equals(item.getSupplier()))
                            if(supplierClient.confirmAccommodation(item.getReservationId())){
                                item.setStatus(OrderStatus.CONFIRMED);
                                orderRepository.save(order);
                            };
                        if ("ticket".equals(item.getSupplier()))
                            if(supplierClient.confirmTicket(item.getReservationId())){
                                item.setStatus(OrderStatus.CONFIRMED);
                                orderRepository.save(order);
                            };
                        if ("transport".equals(item.getSupplier()))
                            if(supplierClient.confirmTransport(item.getReservationId())){
                                item.setStatus(OrderStatus.CONFIRMED);
                                orderRepository.save(order);
                            };
                    }
                }
                boolean allConfirmed = order.getItems().stream().allMatch(item -> item.getStatus()==OrderStatus.CONFIRMED);
                if(allConfirmed){
                    order.setStatus(OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                }
            } else if (anyCancelling) {
                for (OrderItem item : order.getItems()) {
                    if (item.getStatus() == OrderStatus.CANCELLING) {
                        if ("accommodation".equals(item.getSupplier()))
                            supplierClient.cancelAccommodation(item.getReservationId());
                        if ("ticket".equals(item.getSupplier()))
                            supplierClient.cancelTicket(item.getReservationId());
                        if ("transport".equals(item.getSupplier()))
                            supplierClient.cancelTransport(item.getReservationId());
                        item.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                    }
                }
                order.setStatus(OrderStatus.CANCELLED);
            } else if (order.getItems().size() == 3 &&
                       order.getItems().stream().allMatch(item -> item.getStatus() == OrderStatus.PENDING)) {
                // Phase 1 fully succeeded but broker crashed before Phase 2 - complete the commit
                System.out.println("[Recovery] Phase 1 was complete for order " + order.getOrderId() + " — completing Phase 2 (confirm)");
                for (OrderItem item : order.getItems()) {
                    if ("accommodation".equals(item.getSupplier()))
                        supplierClient.confirmAccommodation(item.getReservationId());
                    if ("ticket".equals(item.getSupplier()))
                        supplierClient.confirmTicket(item.getReservationId());
                    if ("transport".equals(item.getSupplier()))
                        supplierClient.confirmTransport(item.getReservationId());
                    item.setStatus(OrderStatus.CONFIRMED);
                }
                order.setStatus(OrderStatus.CONFIRMED);
            } else {
                // Broker crashed during Phase 1 - cancel whatever was reserved
                for (OrderItem item : order.getItems()) {
                    if (item.getStatus() == OrderStatus.PENDING) {
                        if ("accommodation".equals(item.getSupplier()))
                            supplierClient.cancelAccommodation(item.getReservationId());
                        if ("ticket".equals(item.getSupplier()))
                            supplierClient.cancelTicket(item.getReservationId());
                        if ("transport".equals(item.getSupplier()))
                            supplierClient.cancelTransport(item.getReservationId());
                        item.setStatus(OrderStatus.CANCELLED);
                        orderRepository.save(order);
                    }
                }
                order.setStatus(OrderStatus.CANCELLED);
            }
            orderRepository.save(order);
        }
    }

    public Optional<Order> getOrder(int orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrderByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
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
    public Order placeOrder(String customerName, String deliveryAddress, String paymentInfo, int eventId, int ticketId, int accommodationId, int transportId, int quantity) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentInfo(paymentInfo);
        order.setQuantity(quantity);
        order.setStatus(OrderStatus.PENDING);
        order.setAccommodationId(accommodationId);
        order.setTransportId(transportId);
        order.setTicketId(ticketId);
        order.setEventId(eventId);
        orderRepository.save(order);

        // ------------------------------------------------------------------
        // Phase 1: Reserve at all 3 suppliers
        // ------------------------------------------------------------------
        System.out.println("2PC Phase 1: reserving at all suppliers for eventId=" + eventId);

        int accReservationId       = supplierClient.reserveAccommodation(eventId, accommodationId, quantity);
        int ticketReservationId    = supplierClient.reserveTicket(eventId, ticketId, quantity);
        int transportReservationId = supplierClient.reserveTransport(eventId, transportId, quantity);

        boolean phase1Success =
                accReservationId != -1 &&
                ticketReservationId != -1 &&
                transportReservationId != -1;

        if (!phase1Success) {
            // At least one supplier failed → rollback everything
            System.out.println("2PC Phase 1 FAILED — rolling back reservations");
            order.getItems().forEach(item -> item.setStatus(OrderStatus.CANCELLING));
            orderRepository.save(order);
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

        order.getItems().get(0).setStatus(OrderStatus.CONFIRMING);
        orderRepository.save(order);
        boolean accConfirmed = supplierClient.confirmAccommodation(accReservationId);
        if (accConfirmed) {
            order.getItems().get(0).setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        order.getItems().get(1).setStatus(OrderStatus.CONFIRMING);
        orderRepository.save(order);
        boolean ticketConfirmed = supplierClient.confirmTicket(ticketReservationId);
        if (ticketConfirmed) {
            order.getItems().get(1).setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        order.getItems().get(2).setStatus(OrderStatus.CONFIRMING);
        orderRepository.save(order);
        boolean transportConfirmed = supplierClient.confirmTransport(transportReservationId);
        if (transportConfirmed) {
            order.getItems().get(2).setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }

        if (accConfirmed && ticketConfirmed && transportConfirmed) {
            order.setStatus(OrderStatus.CONFIRMED);
            System.out.println("2PC complete: order " + order.getOrderId() + " CONFIRMED");
        } else {
            // Partial confirm failure — cancel what we can (best-effort rollback)
            System.out.println("2PC Phase 2 FAILED — cancelling reservations");
            supplierClient.cancelAccommodation(accReservationId);
            order.getItems().get(0).setStatus(OrderStatus.CANCELLED);
            supplierClient.cancelTicket(ticketReservationId);
            order.getItems().get(1).setStatus(OrderStatus.CANCELLED);
            supplierClient.cancelTransport(transportReservationId);
            order.getItems().get(2).setStatus(OrderStatus.CANCELLED);
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
