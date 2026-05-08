package accommodation_supplier.domain;
public class Accommodation {
    private int id;
    private String name;
    private String location;
    private double pricePerNight;
    private int amountOfAvailableRooms;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public int getAmountOfAvailableRooms() {
        return amountOfAvailableRooms;
    }

    public void setAmountOfAvailableRooms(int amountOfAvailableRooms) {
        this.amountOfAvailableRooms = amountOfAvailableRooms;
    }
}