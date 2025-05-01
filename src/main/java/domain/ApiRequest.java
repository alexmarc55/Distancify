package domain;

import java.sql.Timestamp;

public class ApiRequest {
    private int id;
    private String endpoint;
    private String method;
    private String payload;
    private Timestamp timestamp;
    private String status;
    private String response;

    public ApiRequest() {}

    public ApiRequest(int id, String endpoint, String method, String payload, Timestamp timestamp, String status, String response) {
        this.id = id;
        this.endpoint = endpoint;
        this.method = method;
        this.payload = payload;
        this.timestamp = timestamp;
        this.status = status;
        this.response = response;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}
