package domain;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.util.*;

@Component
public class AccommodationRepository {
    private static final Map<String, Accommodation> accommodations = new HashMap<>();

    public void initData() {
        Accommodation a1 = new Accommodation();
        a1.setId(1);
        a1.setName("Hotel Ibis");
        a1.setLocation("Brussel");
        a1.setPricePerNight(89.99);
        a1.setAmountOfAvailableRooms(10);
        accommodations.put(String.valueOf(a1.getId()), a1);

        Accommodation a2 = new Accommodation();
        a2.setId(2);
        a2.setName("Hotel Marriott");
        a2.setLocation("Gent");
        a2.setPricePerNight(149.99);
        a2.setAmountOfAvailableRooms(5);
        accommodations.put(String.valueOf(a2.getId()), a2);

        Accommodation a3 = new Accommodation();
        a3.setId(3);
        a3.setName("B&B De Zwaan");
        a3.setLocation("Brugge");
        a3.setPricePerNight(65.00);
        a3.setAmountOfAvailableRooms(3);
        accommodations.put(String.valueOf(a3.getId()), a3);
    }
}

