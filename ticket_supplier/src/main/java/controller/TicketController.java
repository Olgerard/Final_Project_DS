package ticket_supplier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ticket_supplier.service.TicketService;
import ticket_supplier.domain.Ticket;
import ticket_supplier.domain.Reservation;
import java.util.Collection;

@RestController
public class TicketController {
    @Autowired
    private TicketService ticketService;

//    @GetMapping("/tickets")
//    ResponseEntity<Collection<Ticket>> getTickets() {return ResponseEntity.ok(ticketService.getAllTickets());}

    @GetMapping("/tickets/{eventId}")
    ResponseEntity<Collection<Ticket>> getTicketsByEvent(@PathVariable int eventId){
        Collection<Ticket> tickets = ticketService.getTicketsByEventId(eventId);
        if(tickets.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(tickets);
    }

//    @GetMapping("/reservations/{id}")
//    ResponseEntity<Reservation> getReservation(@PathVariable int id){
//        Reservation reservation = ticketService.getReservation(id);
//        if (reservation == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(reservation);
//    }

    @PostMapping("/reservations")
    ResponseEntity<Reservation> newReservation(@RequestBody Reservation reservation){
        Reservation createdReservation = ticketService.newReservation(reservation);
        if (createdReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReservation);
    }

    @PutMapping("/reservations/{id}/confirm")
    ResponseEntity<Void> confirmReservation(@PathVariable int id){
        Reservation updatedReservation = ticketService.confirmReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/reservations/{id}/cancel")
    ResponseEntity<Void> cancelReservation(@PathVariable int id){
        Reservation updatedReservation = ticketService.cancelReservation(id);
        if (updatedReservation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
