package accommodation_supplier.controller;

import accommodation_supplier.domain.Accommodation;
import accommodation_supplier.domain.AccommodationRepository;
import accommodation_supplier.domain.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import accommodation_supplier.domain.Reservation;
import java.util.Collection;

@RestController
public class AccommodationController {
    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    AccommodationController(AccommodationRepository accommodationRepository, ReservationRepository reservationRepository) {
        this.accommodationRepository = accommodationRepository;
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/accommodations")
    Collection<Accommodation> getAccommodations() {return accommodationRepository.getAllAccommodations();}

    @PostMapping("/reservations")
    ResponseEntity<Void> newReservation(@RequestBody Reservation reservation){
        Reservation createdReservation = reservationRepository.newReservation(reservation);
        if (createdReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        System.out.println("Created Reservation: " + createdReservation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

