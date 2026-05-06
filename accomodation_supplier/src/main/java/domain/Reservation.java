package domain;

public class Reservation {
    private int id;
    private int accommodationiD;
    private ReservationStatus status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccommodationiD() {
        return accommodationiD;
    }

    public void setAccommodationiD(int accommodationiD) {
        this.accommodationiD = accommodationiD;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
