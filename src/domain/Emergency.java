package domain;

public class Emergency {
    private int id;
    private String city;
    private String county;
    private double latitude;
    private double longitude;
    private int quantity;

    public Emergency(String city, String county, double latitude, double longitude, int quantity) {}

    public Emergency(String city, String county, double latitude, double longitude, int quantity, boolean resolved,) {
        this.city = city;
        this.county = county;
        this.latitude = latitude;
        this.longitude = longitude;
        this.quantity = quantity;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

}
