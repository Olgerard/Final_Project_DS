package broker.service;

import broker.domain.Order;
import broker.domain.OrderItem;
import broker.domain.OrderStatus;
import jakarta.annotation.PostConstruct;
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

    //Temporary testdata
    @PostConstruct
    public void initData() {
        Order order = new Order();
        order.setCustomerName("Alice");
        order.setDeliveryAddress("alice@example.com");
        order.setPaymentInfo("4111111111111111");
        order.setStatus(OrderStatus.CONFIRMED);
        order.getItems().add(new OrderItem("accommodation", 1));
        order.getItems().add(new OrderItem("ticket", 1));
        order.getItems().add(new OrderItem("transport", 1));
        orderRepository.save(order);
    }

    //Recovering Pending requests after broker crash
    @PostConstruct
    public void recoverPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        for (Order order : pendingOrders) {
            boolean anyConfirmed = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderStatus.CONFIRMED);
            boolean anyConfirming = order.getItems().stream().anyMatch(item -> item.getStatus() == OrderStatus.CONFIRMING);
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
                            if(supplierClient.confirmTransport(item.getReservationId()))
                            {
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
            } else {
                // Broker crashed before any item was confirmed
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
