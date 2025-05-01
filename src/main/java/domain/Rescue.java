package domain;

public class Rescue {
    private int id;
    private String county;
    private String city;
    private double latitude;
    private double longitude;
    private int quantity;

    public Rescue() {}

    public Rescue(String county, String city, double latitude, double longitude, int quantity) {
        this.county = county;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.quantity = quantity;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}