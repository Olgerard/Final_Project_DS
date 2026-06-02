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
    public void initData() {
        // Event 1: Tomorrowland (Boom)
        Transport tr1 = new Transport();
        tr1.setEventId(1);
        tr1.setType("Bus");
        tr1.setDeparture("Brussel");
        tr1.setDestination("Boom");
        tr1.setPrice(15.00);
        tr1.setStock(50);
        transportRepository.save(tr1);

        Transport tr2 = new Transport();
        tr2.setEventId(1);
        tr2.setType("Trein");
        tr2.setDeparture("Antwerpen");
        tr2.setDestination("Boom");
        tr2.setPrice(10.00);
        tr2.setStock(80);
        transportRepository.save(tr2);

        // Event 2: Gentse Feesten (Gent)
        Transport tr3 = new Transport();
        tr3.setEventId(2);
        tr3.setType("Bus");
        tr3.setDeparture("Brussel");
        tr3.setDestination("Gent");
        tr3.setPrice(18.00);
        tr3.setStock(60);
        transportRepository.save(tr3);

        Transport tr4 = new Transport();
        tr4.setEventId(2);
        tr4.setType("Trein");
        tr4.setDeparture("Antwerpen");
        tr4.setDestination("Gent");
        tr4.setPrice(12.00);
        tr4.setStock(100);
        transportRepository.save(tr4);

        // Event 3: Rock Werchter (Werchter)
        Transport tr5 = new Transport();
        tr5.setEventId(3);
        tr5.setType("Shuttle");
        tr5.setDeparture("Leuven");
        tr5.setDestination("Werchter");
        tr5.setPrice(8.00);
        tr5.setStock(120);
        transportRepository.save(tr5);

        Transport tr6 = new Transport();
        tr6.setEventId(3);
        tr6.setType("Bus");
        tr6.setDeparture("Brussel");
        tr6.setDestination("Werchter");
        tr6.setPrice(20.00);
        tr6.setStock(70);
        transportRepository.save(tr6);
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
