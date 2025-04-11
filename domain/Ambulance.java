package domain;

public class Ambulance {
    private String location;
    private int quantity;

    public Ambulance() {}

    public Ambulance(String location, int quantity) {
        this.location = location;
        this.quantity = quantity;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
