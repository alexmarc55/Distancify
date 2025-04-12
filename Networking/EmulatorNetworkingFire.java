package Networking;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class EmulatorNetworkingFire extends EmulatorNetworking {
    public List<Map<String, Object>> getAvailableFire() throws Exception {
        String url = BASE_URL + "/fire/search";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<Map<String, Object>> fire = mapper.readValue(response.body(), List.class);
        return fire;
    }

    public void sendDispatchNotification(String sourceCounty, String sourceCity, String targetCounty, String targetCity, int quantity) {
        try {
            String jsonPayload = String.format("{\"sourceCounty\":\"%s\",\"sourceCity\":\"%s\",\"targetCounty\":\"%s\",\"targetCity\":\"%s\",\"quantity\":%d}",
                    sourceCounty, sourceCity, targetCounty, targetCity, quantity);
            String url = BASE_URL + "/fire/dispatch";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Fire Dispatch Notification Response: " + sourceCounty + " | " + sourceCity + " | " + targetCounty + " | " + targetCity + " | " + quantity);

        } catch (Exception e) {
            System.out.println("Error sending fire dispatch notification: " + e.getMessage());
        }
    }
}
