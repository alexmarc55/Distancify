package Networking;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Map<String, Object>> getLocations() throws IOException, InterruptedException {
        String url = BASE_URL + "/locations";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        // Parse the response body as a list of maps
        List<Map<String, Object>> locations = mapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});

        System.out.println("All Locations: " + locations);
        return locations;
    }

    public List<Map<String, Object>> getAmbulances() throws IOException, InterruptedException {
        String url = BASE_URL + "/medical/search";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Content-Type", "application/json") // Dacă API-ul așteaptă acest antet
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        List<Map<String, Object>> locations = mapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> filteredLocations = locations.stream()
                .filter(location -> {
                    // Get the 'quantity' field and check if it's greater than 0
                    Object quantityObj = location.get("quantity");
                    if (quantityObj instanceof Number) {
                        return ((Number) quantityObj).intValue() > 0;
                    }
                    return false; // If quantity is not a number or is missing, exclude this location
                })
                .collect(Collectors.toList());

        System.out.println("All Ambulances: " + filteredLocations);
        return filteredLocations;
    }

    public List<Map<String, Object>> getFire() throws IOException, InterruptedException {
        String url = BASE_URL + "/fire/search";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        List<Map<String, Object>> locations = mapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> filteredLocations = locations.stream()
                .filter(location -> {
                    Object quantityObj = location.get("quantity");
                    if (quantityObj instanceof Number) {
                        return ((Number) quantityObj).intValue() > 0;
                    }
                    return false; // If quantity is not a number or is missing, exclude this location
                })
                .collect(Collectors.toList());

        System.out.println("All Fire: " + filteredLocations);
        return filteredLocations;
    }

    public List<Map<String, Object>> getPolice() throws IOException, InterruptedException {
        String url = BASE_URL + "/police/search";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return null;
        }

        List<Map<String, Object>> locations = mapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
        List<Map<String, Object>> filteredLocations = locations.stream()
                .filter(location -> {
                    Object quantityObj = location.get("quantity");
                    if (quantityObj instanceof Number) {
                        return ((Number) quantityObj).intValue() > 0;
                    }
                    return false; // If quantity is not a number or is missing, exclude this location
                })
                .collect(Collectors.toList());

        System.out.println("All Police: " + filteredLocations);
        return filteredLocations;
    }
}
