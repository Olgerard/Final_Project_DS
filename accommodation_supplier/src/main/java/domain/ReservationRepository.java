package accommodation_supplier.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import accommodation_supplier.domain.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByStatus(accommodation_supplier.domain.ReservationStatus status);
}
