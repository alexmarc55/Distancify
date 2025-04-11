package domain;

import java.sql.Timestamp;

public class ErrorLog {
    private int id;
    private int missed;
    private int overDispatched;
    private Timestamp createdAt;

    public ErrorLog() {}

    public ErrorLog(int id, int missed, int overDispatched, Timestamp createdAt) {
        this.id = id;
        this.missed = missed;
        this.overDispatched = overDispatched;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMissed() { return missed; }
    public void setMissed(int missed) { this.missed = missed; }

    public int getOverDispatched() { return overDispatched; }
    public void setOverDispatched(int overDispatched) { this.overDispatched = overDispatched; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
