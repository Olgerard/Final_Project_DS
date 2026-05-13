package transport_supplier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transport_supplier.service.TransportService;
import transport_supplier.domain.Transport;
import transport_supplier.domain.Reservation;
import java.util.Collection;

@RestController
public class TransportController {
    @Autowired
    private TransportService transportService;

    @GetMapping("/transport")
    ResponseEntity<Collection<Transport>> getTransport() {return ResponseEntity.ok(transportService.getAllTransports());}

    @GetMapping("/transport/{eventId}")
    ResponseEntity<Collection<Transport>> getTransportByEvent(@PathVariable int eventId){
        Collection<Transport> Transports = transportService.getTransportsByEventId(eventId);
        if(Transports.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(Transports);
    }

    @GetMapping("/reservations/{id}")
    ResponseEntity<Reservation> getReservation(@PathVariable int id){
        Reservation reservation = transportService.getReservation(id);
        if (reservation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(reservation);
    }

    @PostMapping("/reservations")
    ResponseEntity<Reservation> newReservation(@RequestBody Reservation reservation){
        Reservation createdReservation = transportService.newReservation(reservation);
        if (createdReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping("/reservations/{id}/confirm")
    ResponseEntity<Void> confirmReservation(@PathVariable int id){
        Reservation updatedReservation = transportService.confirmReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/reservations/{id}/cancel")
    ResponseEntity<Void> cancelReservation(@PathVariable int id){
        Reservation updatedReservation = transportService.cancelReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
