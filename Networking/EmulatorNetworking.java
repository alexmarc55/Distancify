package Networking;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class EmulatorNetworking {
    protected static final String BASE_URL = "http://localhost:5000";
    protected static final ObjectMapper mapper = new ObjectMapper();
    protected static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public EmulatorNetworking(){};

    public Map<String, Object> getNextEmergency() throws Exception {
        String url = BASE_URL + "/calls/next";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        Map<String, Object> emergency = mapper.readValue(response.body(), Map.class);

        System.out.println("Next Emergency: " + emergency);
        return emergency;
    }

    public void stopSimulation() throws IOException, InterruptedException {
        String url = BASE_URL + "/control/stop";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Stop Response: " + response.body());
    }

    public void resetSimulation(String seed, int targetDispatches, int maxActiveCalls) throws Exception {
        String url = BASE_URL + "/control/reset?seed=" + seed + "&targetDispatches=" + targetDispatches + "&maxActiveCalls=" + maxActiveCalls;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Reset Response: " + response.body());
    }
}
