package controller;

import domain.AccommodationRepository;
import domain.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccommodationController {
    private final AccommodationRepository accommodationRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    AccommodationController(AccommodationRepository accommodationRepository, ReservationRepository reservationRepository) {
        this.accommodationRepository = accommodationRepository;
        this.reservationRepository = reservationRepository;
    }
}

