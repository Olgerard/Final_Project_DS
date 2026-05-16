package broker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int itemId;
    private String supplier;       // "accommodation", "ticket", "transport"
    private int reservationId;     // id returned by the supplier after phase 1

    public OrderItem() {}

    public OrderItem(String supplier, int reservationId) {
        this.supplier = supplier;
        this.reservationId = reservationId;
    }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }
}
