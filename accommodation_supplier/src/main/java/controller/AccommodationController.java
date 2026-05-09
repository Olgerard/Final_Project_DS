package accommodation_supplier.controller;

import accommodation_supplier.domain.Accommodation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import accommodation_supplier.domain.Reservation;
import java.util.Collection;
import accommodation_supplier.service.AccommodationService;

@RestController
public class AccommodationController {
    @Autowired
    private AccommodationService accommodationService;

    @GetMapping("/accommodations")
    ResponseEntity<Collection<Accommodation>> getAccommodations() {return ResponseEntity.ok(accommodationService.getAllAccommodations());}

    @GetMapping("/reservations/{id}")
    ResponseEntity<Reservation> getReservation(@PathVariable int id){
        Reservation reservation = accommodationService.getReservation(id);
        if (reservation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations")
    ResponseEntity<Reservation> newReservation(@RequestBody Reservation reservation){
        Reservation createdReservation = accommodationService.newReservation(reservation);
        if (createdReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping("/reservations/{id}/confirm")
    ResponseEntity<Void> confirmReservation(@PathVariable int id){
        Reservation updatedReservation = accommodationService.confirmReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/reservations/{id}/cancel")
    ResponseEntity<Void> cancelReservation(@PathVariable int id){
        Reservation updatedReservation = accommodationService.cancelReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

