package accommodation_supplier.domain;
import accommodation_supplier.domain.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public interface AccommodationRepository extends JpaRepository<Accommodation, Integer> {
    List<Accommodation> findByEventId(int eventId);
}

