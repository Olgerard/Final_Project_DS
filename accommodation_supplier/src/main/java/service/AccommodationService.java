package accommodation_supplier.service;

import accommodation_supplier.domain.AccommodationRepository;
import accommodation_supplier.domain.ReservationStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import accommodation_supplier.domain.ReservationRepository;
import accommodation_supplier.domain.Accommodation;
import accommodation_supplier.domain.Reservation;

import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;

@Service
public class AccommodationService {
    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @PostConstruct
    public void recoverReservations() {
        List<Reservation> reservations = reservationRepository.findByStatus(ReservationStatus.RESERVED);
        for (Reservation reservation : reservations) {
            cancelReservation(reservation.getId());
        }
    }
    @PostConstruct
    public void init() {
        if (accommodationRepository.count() == 0) {
            accommodationRepository.saveAll(List.of(
                    make(1, "Camping Dreamville",        "Boom",     45.00, 200),
                    make(1, "Camping Sunrise",           "Boom",     29.99, 150),
                    make(2, "Camping Kouter",            "Gent",     25.00, 100),
                    make(2, "Camping Gravensteen",       "Gent",     19.99,  1),
                    make(3, "Camping Werchter Boutique", "Werchter", 35.00, 120),
                    make(3, "Camping Festivalpark",      "Werchter", 22.99,  90)
            ));
        }
        reservationRepository.findByStatus(ReservationStatus.RESERVED)
                .forEach(r -> cancelReservation(r.getId()));
    }

    private Accommodation make(int eventId, String name, String location, double price, int stock) {
        Accommodation a = new Accommodation();
        a.setEventId(eventId); a.setName(name); a.setLocation(location);
        a.setPrice(price); a.setStock(stock);
        return a;
    }

    @Transactional
    public Reservation newReservation(Reservation reservation) {

        //Test supplier failure before confirming a reservation
        //System.exit(1);

        Accommodation accommodation = accommodationRepository.findById(reservation.getAccommodationId()).orElse(null);
        if (accommodation == null || accommodation.getStock() < reservation.getQuantity()) {
            return null;
        }
        accommodation.setStock(accommodation.getStock() - reservation.getQuantity());
        reservation.setStatus(ReservationStatus.RESERVED);
        accommodationRepository.save(accommodation);
        //Test supplier failure before confirming a reservation, but after changing stock
        //System.exit(1);
        return reservationRepository.save(reservation);
    }

    public Reservation confirmReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);

        //Supplier failure before sending confirmation
        //System.exit(1);

        if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED) return null;
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) return null;
        if (reservation.getStatus() == ReservationStatus.CANCELLED) return reservation;
        Accommodation accommodation = accommodationRepository.findById(reservation.getAccommodationId()).orElse(null);
        if (accommodation != null) {
            accommodation.setStock(accommodation.getStock() + reservation.getQuantity());
            accommodationRepository.save(accommodation);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public Collection<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    public Collection<Accommodation> getAccommodationsByEventId(int eventId) {
        return accommodationRepository.findByEventId(eventId);
    }

    public Reservation getReservation(int id) {
        return reservationRepository.findById(id).orElse(null);
    }

}
