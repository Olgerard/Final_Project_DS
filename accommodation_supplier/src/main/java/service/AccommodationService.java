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
