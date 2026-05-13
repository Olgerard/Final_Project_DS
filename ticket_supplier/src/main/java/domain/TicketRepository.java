package ticket_supplier.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ticket_supplier.domain.Ticket;

import java.util.List;

@Component
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByEventId(int eventId);
}
