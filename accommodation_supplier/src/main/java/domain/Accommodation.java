package accommodation_supplier.domain;
public class Accommodation {
    private int id;
    private String name;
    private String location;
    private double price;
    private int stock;

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
        return price;
    }

    public void setPricePerNight(double pricePerNight) {
        this.price = pricePerNight;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int amountOfAvailableRooms) {
        this.stock = amountOfAvailableRooms;
    }
}