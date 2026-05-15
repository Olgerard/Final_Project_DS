package com.example.webcontent;

public class OrderItem {
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
