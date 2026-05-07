package accommodation_supplier.controller;

import domain.Accommodation;
import accommodation_supplier.domain.AccommodationRepository;
import accommodation_supplier.domain.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

