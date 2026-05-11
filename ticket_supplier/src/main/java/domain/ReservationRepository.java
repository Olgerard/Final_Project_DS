package ticket_supplier.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ticket_supplier.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

}
