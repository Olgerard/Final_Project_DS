package accommodation_supplier.domain;

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
        reservations.put(String.valueOf(reservation.getId()), reservation);
        return reservation;
    }
}
