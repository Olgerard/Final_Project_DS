package broker.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import broker.domain.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
}
