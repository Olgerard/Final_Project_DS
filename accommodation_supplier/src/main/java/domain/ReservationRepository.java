package accommodation_supplier.domain;

import domain.ReservationStatus;
import org.springframework.stereotype.Component;
import accommodation_supplier.domain.Reservation;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReservationRepository {
    private static final Map<String, Reservation> reservations = new HashMap<>();

    public Reservation newReservation(Reservation reservation){
        Assert.notNull(reservation, "The reservation must not be null");
        reservation.setId(reservations.size() + 1);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservations.put(String.valueOf(reservation.getId()), reservation);
        return reservation;
    }

    public Reservation confirmReservation(int id){
        Reservation reservation = reservations.get(String.valueOf(id));
        Assert.notNull(reservation, "The id must not be null");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    public Reservation cancelReservation(int id){
        Reservation reservation = reservations.get(String.valueOf(id));
        Assert.notNull(reservation, "The id must not be null");
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }

    public Reservation getReservation(int id){
        return reservations.get(String.valueOf(id));
    }
}
