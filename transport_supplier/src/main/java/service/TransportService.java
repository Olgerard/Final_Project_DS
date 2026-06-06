package transport_supplier.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import transport_supplier.domain.ReservationRepository;
import transport_supplier.domain.TransportRepository;
import transport_supplier.domain.Transport;
import transport_supplier.domain.Reservation;
import transport_supplier.domain.ReservationStatus;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.List;

@Service
public class TransportService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TransportRepository transportRepository;

    @PostConstruct
    public void recoverReservations() {
        List<Reservation> reservations = reservationRepository.findByStatus(ReservationStatus.RESERVED);
        for (Reservation reservation : reservations) {
            cancelReservation(reservation.getId());
        }
    }
    @PostConstruct
    public void init() {
        if (transportRepository.count() == 0) {
            transportRepository.saveAll(List.of(
                    make(1, "Bus",     "Brussel",   "Boom",     15.00,  50),
                    make(1, "Trein",   "Antwerpen", "Boom",     10.00,  80),
                    make(2, "Bus",     "Brussel",   "Gent",     18.00,  60),
                    make(2, "Trein",   "Antwerpen", "Gent",     12.00, 100),
                    make(3, "Shuttle", "Leuven",    "Werchter",  8.00, 120),
                    make(3, "Bus",     "Brussel",   "Werchter", 20.00,  70)
            ));
        }
        reservationRepository.findByStatus(ReservationStatus.RESERVED)
                .forEach(r -> cancelReservation(r.getId()));
    }

    private Transport make(int eventId, String type, String departure, String destination, double price, int stock) {
        Transport t = new Transport();
        t.setEventId(eventId); t.setType(type); t.setDeparture(departure);
        t.setDestination(destination); t.setPrice(price); t.setStock(stock);
        return t;
    }

    @Transactional
    public Reservation newReservation(Reservation reservation) {
        Transport transport = transportRepository.findById(reservation.getTransportId()).orElse(null);
        if (transport == null || transport.getStock() < reservation.getQuantity()) {
            return null;
        }
        transport.setStock(transport.getStock() - reservation.getQuantity());
        transportRepository.save(transport);
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
        Transport transport = transportRepository.findById(reservation.getTransportId()).orElse(null);
        if (transport != null) {
            transport.setStock(transport.getStock() + reservation.getQuantity());
            transportRepository.save(transport);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public Collection<Transport> getAllTransports() {
        return transportRepository.findAll();
    }

    public Collection<Transport> getTransportsByEventId(int eventId) {
        return transportRepository.findByEventId(eventId);
    }

    public Reservation getReservation(int id) {
        return reservationRepository.findById(id).orElse(null);
    }

}
