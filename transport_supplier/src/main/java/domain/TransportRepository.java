package transport_supplier.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import transport_supplier.domain.Transport;

import java.util.List;

@Component
public interface TransportRepository extends JpaRepository<Transport, Integer> {
    List<Transport> findByEventId(int eventId);
}
