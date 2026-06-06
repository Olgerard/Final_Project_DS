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
