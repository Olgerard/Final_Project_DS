package transport_supplier.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import transport_supplier.domain.ReservationRepository;
import transport_supplier.domain.TransportRepository;

@Service
public class TransportService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TransportRepository transportRepository;
}
