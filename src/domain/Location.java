package domain;

public class Location {
    private String name;
    private String county;
    private double latitude;
    private double longitude;

    public Location() {}

    public Location(String name, String county, double latitude, double longitude) {
        this.name = name;
        this.county = county;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
