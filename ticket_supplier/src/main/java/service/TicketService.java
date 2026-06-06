package ticket_supplier.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ticket_supplier.domain.TicketRepository;
import ticket_supplier.domain.ReservationRepository;
import ticket_supplier.domain.Ticket;
import ticket_supplier.domain.Reservation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import java.util.List;

import ticket_supplier.domain.ReservationStatus;

@Service
public class TicketService {
    @Autowired
    private TicketRepository ticketRepository;

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
    public void init() {
        if (ticketRepository.count() == 0) {
            ticketRepository.saveAll(List.of(
                    make(1, "Tomorrowland",       "Boom",     120.00, 500),
                    make(1, "Tomorrowland VIP",   "Boom",     250.00, 100),
                    make(2, "Gentse Feesten",     "Gent",      25.00, 300),
                    make(2, "Gentse Feesten VIP", "Gent",      75.00,  50),
                    make(3, "Rock Werchter",      "Werchter",  95.00, 400),
                    make(3, "Rock Werchter VIP",  "Werchter", 200.00,  75)
            ));
        }
        reservationRepository.findByStatus(ReservationStatus.RESERVED)
                .forEach(r -> cancelReservation(r.getId()));
    }

    private Ticket make(int eventId, String name, String location, double price, int stock) {
        Ticket t = new Ticket();
        t.setEventId(eventId); t.setEventName(name); t.setLocation(location);
        t.setPrice(price); t.setStock(stock);
        return t;
    }

    @Transactional
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

    @Transactional
    public Reservation cancelReservation(int id) {
        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) return null;
        if (reservation.getStatus() == ReservationStatus.CANCELLED) return reservation;
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
