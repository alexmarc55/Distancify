package domain;

import java.sql.Timestamp;

public class Dispatch {
    private int id;
    private String sourceCity;
    private String sourceCounty;
    private String targetCity;
    private String targetCounty;
    private int quantity;
    private Timestamp timestamp;

    public Dispatch() {}

    public Dispatch(int id, String sourceCity, String sourceCounty, String targetCity, String targetCounty, int quantity, Timestamp timestamp) {
        this.id = id;
        this.sourceCity = sourceCity;
        this.sourceCounty = sourceCounty;
        this.targetCity = targetCity;
        this.targetCounty = targetCounty;
        this.quantity = quantity;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSourceCity() { return sourceCity; }
    public void setSourceCity(String sourceCity) { this.sourceCity = sourceCity; }

    public String getSourceCounty() { return sourceCounty; }
    public void setSourceCounty(String sourceCounty) { this.sourceCounty = sourceCounty; }

    public String getTargetCity() { return targetCity; }
    public void setTargetCity(String targetCity) { this.targetCity = targetCity; }

    public String getTargetCounty() { return targetCounty; }
    public void setTargetCounty(String targetCounty) { this.targetCounty = targetCounty; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
