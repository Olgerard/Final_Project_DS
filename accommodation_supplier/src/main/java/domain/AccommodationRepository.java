package accommodation_supplier.domain;
import accommodation_supplier.domain.Accommodation;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.util.*;

@Component
public class AccommodationRepository {
    private static final Map<String, Accommodation> accommodations = new HashMap<>();

    @PostConstruct
    public void initData() {
        Accommodation a1 = new Accommodation();
        a1.setId(1);
        a1.setName("Camping Sunrise");
        a1.setLocation("Brussel");
        a1.setPricePerNight(29.99);
        a1.setStock(50);
        accommodations.put(String.valueOf(a1.getId()), a1);

        Accommodation a2 = new Accommodation();
        a2.setId(2);
        a2.setName("Camping Moonlight");
        a2.setLocation("Gent");
        a2.setPricePerNight(24.99);
        a2.setStock(30);
        accommodations.put(String.valueOf(a2.getId()), a2);

        Accommodation a3 = new Accommodation();
        a3.setId(3);
        a3.setName("Camping Stardust");
        a3.setLocation("Antwerpen");
        a3.setPricePerNight(19.99);
        a3.setStock(20);
        accommodations.put(String.valueOf(a3.getId()), a3);
    }

    public Collection<Accommodation> getAllAccommodations() {return accommodations.values();}
}

