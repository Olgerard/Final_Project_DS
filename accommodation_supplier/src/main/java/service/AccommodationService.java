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

@Service
public class AccommodationService {
    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @PostConstruct
    public void initData() {
        Accommodation a1 = new Accommodation();
        a1.setName("Camping Sunrise");
        a1.setLocation("Brussel");
        a1.setPricePerNight(29.99);
        a1.setStock(50);
        accommodationRepository.save(a1);

        Accommodation a2 = new Accommodation();
        a2.setName("Camping Moonlight");
        a2.setLocation("Gent");
        a2.setPricePerNight(24.99);
        a2.setStock(30);
        accommodationRepository.save(a2);

        Accommodation a3 = new Accommodation();
        a3.setName("Camping Stardust");
        a3.setLocation("Antwerpen");
        a3.setPricePerNight(19.99);
        a3.setStock(20);
        accommodationRepository.save(a3);
    }

    public Reservation newReservation(Reservation reservation) {
        Accommodation accommodation = accommodationRepository.findById(reservation.getAccommodationId()).orElse(null);
        if (accommodation == null || accommodation.getStock() < reservation.getQuantity()) {
            return null;
        }
        accommodation.setStock(accommodation.getStock() - reservation.getQuantity());
        accommodationRepository.save(accommodation);
        reservation.setStatus(ReservationStatus.RESERVED);
        return reservationRepository.save(reservation);
    }

    public Reservation confirmReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) return null;
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) return null;
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

    public Reservation getReservation(int id) {
        return reservationRepository.findById(id).orElse(null);
    }

}
