package accommodation_supplier.service;

import accommodation_supplier.domain.AccommodationRepository;
import accommodation_supplier.domain.ReservationStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import accommodation_supplier.domain.ReservationRepository;
import accommodation_supplier.domain.Accommodation;
import accommodation_supplier.domain.Reservation;

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
    public void initData() {
        Accommodation a1 = new Accommodation();
        a1.setEventId(1);
        a1.setName("Camping Dreamville");
        a1.setLocation("Boom");
        a1.setPrice(45.00);
        a1.setStock(200);
        accommodationRepository.save(a1);

        Accommodation a2 = new Accommodation();
        a2.setEventId(1);
        a2.setName("Camping Sunrise");
        a2.setLocation("Boom");
        a2.setPrice(29.99);
        a2.setStock(150);
        accommodationRepository.save(a2);

        Accommodation a3 = new Accommodation();
        a3.setEventId(2);
        a3.setName("Camping Kouter");
        a3.setLocation("Gent");
        a3.setPrice(25.00);
        a3.setStock(100);
        accommodationRepository.save(a3);

        Accommodation a4 = new Accommodation();
        a4.setEventId(2);
        a4.setName("Camping Gravensteen");
        a4.setLocation("Gent");
        a4.setPrice(19.99);
        a4.setStock(80);
        accommodationRepository.save(a4);

        Accommodation a5 = new Accommodation();
        a5.setEventId(3);
        a5.setName("Camping Werchter Boutique");
        a5.setLocation("Werchter");
        a5.setPrice(35.00);
        a5.setStock(120);
        accommodationRepository.save(a5);

        Accommodation a6 = new Accommodation();
        a6.setEventId(3);
        a6.setName("Camping Festivalpark");
        a6.setLocation("Werchter");
        a6.setPrice(22.99);
        a6.setStock(90);
        accommodationRepository.save(a6);
    }

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
