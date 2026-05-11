package ticket_supplier.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ticket_supplier.domain.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {

}
