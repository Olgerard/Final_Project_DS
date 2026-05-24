package broker.domain;

import jakarta.persistence.*;
import broker.domain.OrderStatus;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int itemId;
    private String supplier;       // "accommodation", "ticket", "transport"
    private int reservationId;     // id returned by the supplier after phase 1
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public OrderItem() {}

    public OrderItem(String supplier, int reservationId) {
        this.supplier = supplier;
        this.reservationId = reservationId;
        this.status = OrderStatus.PENDING;
    }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
}
