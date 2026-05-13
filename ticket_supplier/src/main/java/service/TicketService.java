package ticket_supplier.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ticket_supplier.domain.TicketRepository;
import ticket_supplier.domain.ReservationRepository;
import ticket_supplier.domain.Ticket;
import ticket_supplier.domain.Reservation;
import java.util.Collection;
import ticket_supplier.domain.ReservationStatus;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @PostConstruct
    public void initData() {
        // Event 1: Tomorrowland
        Ticket t1 = new Ticket();
        t1.setEventId(1);
        t1.setEventName("Tomorrowland");
        t1.setLocation("Boom");
        t1.setPrice(120.00);
        t1.setStock(500);
        ticketRepository.save(t1);

        Ticket t2 = new Ticket();
        t2.setEventId(1);
        t2.setEventName("Tomorrowland VIP");
        t2.setLocation("Boom");
        t2.setPrice(250.00);
        t2.setStock(100);
        ticketRepository.save(t2);

        // Event 2: Gentse Feesten
        Ticket t3 = new Ticket();
        t3.setEventId(2);
        t3.setEventName("Gentse Feesten");
        t3.setLocation("Gent");
        t3.setPrice(25.00);
        t3.setStock(300);
        ticketRepository.save(t3);

        Ticket t4 = new Ticket();
        t4.setEventId(2);
        t4.setEventName("Gentse Feesten VIP");
        t4.setLocation("Gent");
        t4.setPrice(75.00);
        t4.setStock(50);
        ticketRepository.save(t4);

        // Event 3: Rock Werchter
        Ticket t5 = new Ticket();
        t5.setEventId(3);
        t5.setEventName("Rock Werchter");
        t5.setLocation("Werchter");
        t5.setPrice(95.00);
        t5.setStock(400);
        ticketRepository.save(t5);

        Ticket t6 = new Ticket();
        t6.setEventId(3);
        t6.setEventName("Rock Werchter VIP");
        t6.setLocation("Werchter");
        t6.setPrice(200.00);
        t6.setStock(75);
        ticketRepository.save(t6);
    }
    public Reservation newReservation(Reservation reservation) {
        Ticket Ticket = ticketRepository.findById(reservation.getTicketId()).orElse(null);
        if (Ticket == null || Ticket.getStock() < reservation.getQuantity()) {
            return null;
        }
        Ticket.setStock(Ticket.getStock() - reservation.getQuantity());
        ticketRepository.save(Ticket);
        reservation.setStatus(ReservationStatus.RESERVED);
        return reservationRepository.save(reservation);
    }

    public Reservation confirmReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED) return null;
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED) return null;
        Ticket Ticket = ticketRepository.findById(reservation.getTicketId()).orElse(null);
        if (Ticket != null) {
            Ticket.setStock(Ticket.getStock() + reservation.getQuantity());
            ticketRepository.save(Ticket);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public Collection<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Collection<Ticket> getTicketsByEventId(int eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    public Reservation getReservation(int id) {
        return reservationRepository.findById(id).orElse(null);
    }

}
