package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import Networking.*;
import Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import repository.*;

public class Main {
    private static Properties props = new Properties();

    public static void main(String[] args) throws Exception {
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            System.out.println("Cannot find db.config: " + e);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        //Repository
        RepositoryLocation repositoryLocation = new RepositoryLocation(props);
        RepositoryEmergency repositoryEmergency = new RepositoryEmergency(props);
        RepositoryDispatch repositoryDispatch = new RepositoryDispatch(props);
        RepositoryAmbulances repositoryAmbulances = new RepositoryAmbulances(props);
        RepositoryFire repositoryFire = new RepositoryFire(props);
        RepositoryPolice repositoryPolice = new RepositoryPolice(props);
        RepositoryRescue repositoryRescue = new RepositoryRescue(props);
        RepositoryUtility repositoryUtility = new RepositoryUtility(props);


        //Emulator Networking
        EmulatorNetworking emulatorNetworking = new EmulatorNetworking();
        EmulatorNetworkingAmbulances emulatorNetworkingAmbulances = new EmulatorNetworkingAmbulances();
        EmulatorNetworkingPolice emulatorNetworkingPolice = new EmulatorNetworkingPolice();
        EmulatorNetworkingFire emulatorNetworkingFire = new EmulatorNetworkingFire();
        EmulatorNetworkingUtility emulatorNetworkingUtility = new EmulatorNetworkingUtility();
        EmulatorNetworkingRescue emulatorNetworkingRescue = new EmulatorNetworkingRescue();


        //Service
        EmulatorService emulatorService = new EmulatorService(emulatorNetworking, emulatorNetworkingAmbulances, emulatorNetworkingPolice, emulatorNetworkingFire, emulatorNetworkingRescue, emulatorNetworkingUtility,true);
        LocationService locationService = new LocationService(repositoryLocation, emulatorNetworking);
        AmbulanceService ambulanceService = new AmbulanceService(repositoryAmbulances, emulatorNetworking);
        FireService fireService = new FireService(repositoryFire, emulatorNetworking);
        PoliceService policeService = new PoliceService(repositoryPolice, emulatorNetworking);
        RescueService rescueService = new RescueService(repositoryRescue, emulatorNetworking);
        UtilityService utilityService = new UtilityService(repositoryUtility, emulatorNetworking);

        // Definește un handler pentru ruta "/api/emergency"
        server.createContext("/api/emergency", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm un obiect Map<String, Object> pentru datele de tip emergency
                Map<String, Object> emergencyData = null;
                try {
                    emergencyData = emulatorService.getNextEmergencyForFrontend();
                    System.out.println("Next Emergency for Frontend: " + emergencyData);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (emergencyData == null) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No emergency available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(emergencyData);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/ambulances", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm o listă pentru datele despre ambulante
                List<Map<String, Object>> ambulances = null;
                try {
                    ambulances = emulatorNetworking.getAmbulances();
                    System.out.println("Ambulances for Frontend: " + ambulances);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (ambulances == null || ambulances.isEmpty()) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No ambulances available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(ambulances);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/police", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm o listă pentru datele despre ambulante
                List<Map<String, Object>> police = null;
                try {
                    police = emulatorNetworking.getPolice();
                    System.out.println("Police for Frontend: " + police);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (police == null || police.isEmpty()) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No police available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(police);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/fire", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm o listă pentru datele despre ambulante
                List<Map<String, Object>> fire = null;
                try {
                    fire = emulatorNetworking.getFire();
                    System.out.println("Fire for Frontend: " + fire);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (fire == null || fire.isEmpty()) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No fire available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(fire);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/rescue", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm o listă pentru datele despre ambulante
                List<Map<String, Object>> rescue = null;
                try {
                    rescue = emulatorNetworking.getRescue();
                    System.out.println("Rescue for Frontend: " + rescue);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (rescue == null || rescue.isEmpty()) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No Rescue available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(rescue);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/utility", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Creăm o listă pentru datele despre ambulante
                List<Map<String, Object>> utility = null;
                try {
                    utility = emulatorNetworking.getUtility();
                    System.out.println("Utility for Frontend: " + utility);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonResponse;

                if (utility == null || utility.isEmpty()) {
                    Map<String, Object> noData = new HashMap<>();
                    noData.put("message", "No Utility available");
                    jsonResponse = objectMapper.writeValueAsString(noData);
                } else {
                    jsonResponse = objectMapper.writeValueAsString(utility);
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }
            }
        });

        server.createContext("/api/dispatch/ambulance", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Verificăm dacă metoda este POST
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Citim corpul cererii
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Citim întregul conținut al corpului cererii
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parseați corpul cererii JSON într-un obiect Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> requestData = null;
                    try {
                        requestData = objectMapper.readValue(requestBody.toString(), Map.class);
                    } catch (IOException e) {
                        // Dacă nu reușim să parsăm JSON-ul, returnăm un mesaj de eroare
                        String errorResponse = "{\"message\": \"Invalid JSON format\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Extragem datele necesare din requestData
                    String sourceCounty = (String) requestData.get("sourceCounty");
                    String sourceCity = (String) requestData.get("sourceCity");
                    String targetCounty = (String) requestData.get("targetCounty");
                    String targetCity = (String) requestData.get("targetCity");
                    Integer quantity = (Integer) requestData.get("quantity");

                    // Verificăm dacă există valori valabile pentru toate câmpurile
                    if (sourceCounty == null || sourceCity == null || targetCounty == null || targetCity == null || quantity == null) {
                        String errorResponse = "{\"message\": \"Missing required fields\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Instanțiem emulatorul și trimitem notificarea de dispatch
                    emulatorNetworkingAmbulances.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, quantity);
                    emulatorService.deleteFromPending(targetCounty, targetCity, quantity, "Medical");
                    // Pregătim răspunsul
                    String jsonResponse = "{\"message\": \"Dispatch received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);  // 200 OK

                    // Trimitim răspunsul înapoi clientului
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes());
                    }
                } else {
                    // Dacă metoda nu este POST, returnăm un mesaj de eroare
                    String errorResponse = "{\"message\": \"Only POST method is supported for this endpoint\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, errorResponse.getBytes().length);  // 405 Method Not Allowed
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }
                }
            }
        });

        server.createContext("/api/dispatch/police", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Verificăm dacă metoda este POST
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Citim corpul cererii
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Citim întregul conținut al corpului cererii
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parseați corpul cererii JSON într-un obiect Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> requestData = null;
                    try {
                        requestData = objectMapper.readValue(requestBody.toString(), Map.class);
                    } catch (IOException e) {
                        // Dacă nu reușim să parsăm JSON-ul, returnăm un mesaj de eroare
                        String errorResponse = "{\"message\": \"Invalid JSON format\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Extragem datele necesare din requestData
                    String sourceCounty = (String) requestData.get("sourceCounty");
                    String sourceCity = (String) requestData.get("sourceCity");
                    String targetCounty = (String) requestData.get("targetCounty");
                    String targetCity = (String) requestData.get("targetCity");
                    Integer quantity = (Integer) requestData.get("quantity");

                    // Verificăm dacă există valori valabile pentru toate câmpurile
                    if (sourceCounty == null || sourceCity == null || targetCounty == null || targetCity == null || quantity == null) {
                        String errorResponse = "{\"message\": \"Missing required fields\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Instanțiem emulatorul și trimitem notificarea de dispatch
                    emulatorNetworkingPolice.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, quantity);
                    emulatorService.deleteFromPending(targetCounty, targetCity, quantity, "Police");

                    // Pregătim răspunsul
                    String jsonResponse = "{\"message\": \"Dispatch received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);  // 200 OK

                    // Trimitim răspunsul înapoi clientului
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes());
                    }
                } else {
                    // Dacă metoda nu este POST, returnăm un mesaj de eroare
                    String errorResponse = "{\"message\": \"Only POST method is supported for this endpoint\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, errorResponse.getBytes().length);  // 405 Method Not Allowed
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }
                }
            }
        });

        server.createContext("/api/dispatch/fire", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Verificăm dacă metoda este POST
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Citim corpul cererii
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Citim întregul conținut al corpului cererii
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parseați corpul cererii JSON într-un obiect Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> requestData = null;
                    try {
                        requestData = objectMapper.readValue(requestBody.toString(), Map.class);
                    } catch (IOException e) {
                        // Dacă nu reușim să parsăm JSON-ul, returnăm un mesaj de eroare
                        String errorResponse = "{\"message\": \"Invalid JSON format\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Extragem datele necesare din requestData
                    String sourceCounty = (String) requestData.get("sourceCounty");
                    String sourceCity = (String) requestData.get("sourceCity");
                    String targetCounty = (String) requestData.get("targetCounty");
                    String targetCity = (String) requestData.get("targetCity");
                    Integer quantity = (Integer) requestData.get("quantity");

                    // Verificăm dacă există valori valabile pentru toate câmpurile
                    if (sourceCounty == null || sourceCity == null || targetCounty == null || targetCity == null || quantity == null) {
                        String errorResponse = "{\"message\": \"Missing required fields\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Instanțiem emulatorul și trimitem notificarea de dispatch
                    emulatorNetworkingFire.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, quantity);
                    emulatorService.deleteFromPending(targetCounty, targetCity, quantity, "Fire");

                    // Pregătim răspunsul
                    String jsonResponse = "{\"message\": \"Dispatch received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);  // 200 OK

                    // Trimitim răspunsul înapoi clientului
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes());
                    }
                } else {
                    // Dacă metoda nu este POST, returnăm un mesaj de eroare
                    String errorResponse = "{\"message\": \"Only POST method is supported for this endpoint\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, errorResponse.getBytes().length);  // 405 Method Not Allowed
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }
                }
            }
        });

        server.createContext("/api/dispatch/rescue", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Verificăm dacă metoda este POST
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Citim corpul cererii
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Citim întregul conținut al corpului cererii
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parseați corpul cererii JSON într-un obiect Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> requestData = null;
                    try {
                        requestData = objectMapper.readValue(requestBody.toString(), Map.class);
                    } catch (IOException e) {
                        // Dacă nu reușim să parsăm JSON-ul, returnăm un mesaj de eroare
                        String errorResponse = "{\"message\": \"Invalid JSON format\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Extragem datele necesare din requestData
                    String sourceCounty = (String) requestData.get("sourceCounty");
                    String sourceCity = (String) requestData.get("sourceCity");
                    String targetCounty = (String) requestData.get("targetCounty");
                    String targetCity = (String) requestData.get("targetCity");
                    Integer quantity = (Integer) requestData.get("quantity");

                    // Verificăm dacă există valori valabile pentru toate câmpurile
                    if (sourceCounty == null || sourceCity == null || targetCounty == null || targetCity == null || quantity == null) {
                        String errorResponse = "{\"message\": \"Missing required fields\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Instanțiem emulatorul și trimitem notificarea de dispatch
                    emulatorNetworkingRescue.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, quantity);
                    emulatorService.deleteFromPending(targetCounty, targetCity, quantity, "Rescue");

                    // Pregătim răspunsul
                    String jsonResponse = "{\"message\": \"Dispatch received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);  // 200 OK

                    // Trimitim răspunsul înapoi clientului
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes());
                    }
                } else {
                    // Dacă metoda nu este POST, returnăm un mesaj de eroare
                    String errorResponse = "{\"message\": \"Only POST method is supported for this endpoint\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, errorResponse.getBytes().length);  // 405 Method Not Allowed
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }
                }
            }
        });

        server.createContext("/api/dispatch/utility", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                // Permitem cereri din orice origine (poți înlocui "*" cu adresa frontend-ului tău pentru securitate mai mare)
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

                // Dacă este o cerere OPTIONS (pentru CORS preflight)
                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(200, -1);  // 200 OK, fără corp de răspuns
                    return;
                }

                // Verificăm dacă metoda este POST
                if ("POST".equals(exchange.getRequestMethod())) {
                    // Citim corpul cererii
                    InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Citim întregul conținut al corpului cererii
                    StringBuilder requestBody = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    // Parseați corpul cererii JSON într-un obiect Map
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> requestData = null;
                    try {
                        requestData = objectMapper.readValue(requestBody.toString(), Map.class);
                    } catch (IOException e) {
                        // Dacă nu reușim să parsăm JSON-ul, returnăm un mesaj de eroare
                        String errorResponse = "{\"message\": \"Invalid JSON format\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Extragem datele necesare din requestData
                    String sourceCounty = (String) requestData.get("sourceCounty");
                    String sourceCity = (String) requestData.get("sourceCity");
                    String targetCounty = (String) requestData.get("targetCounty");
                    String targetCity = (String) requestData.get("targetCity");
                    Integer quantity = (Integer) requestData.get("quantity");

                    // Verificăm dacă există valori valabile pentru toate câmpurile
                    if (sourceCounty == null || sourceCity == null || targetCounty == null || targetCity == null || quantity == null) {
                        String errorResponse = "{\"message\": \"Missing required fields\"}";
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(400, errorResponse.getBytes().length);  // 400 Bad Request
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(errorResponse.getBytes());
                        }
                        return;
                    }

                    // Instanțiem emulatorul și trimitem notificarea de dispatch
                    emulatorNetworkingUtility.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, quantity);
                    emulatorService.deleteFromPending(targetCounty, targetCity, quantity, "Utility");

                    // Pregătim răspunsul
                    String jsonResponse = "{\"message\": \"Dispatch received successfully\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);  // 200 OK

                    // Trimitim răspunsul înapoi clientului
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(jsonResponse.getBytes());
                    }
                } else {
                    // Dacă metoda nu este POST, returnăm un mesaj de eroare
                    String errorResponse = "{\"message\": \"Only POST method is supported for this endpoint\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, errorResponse.getBytes().length);  // 405 Method Not Allowed
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(errorResponse.getBytes());
                    }
                }
            }
        });


        // Pornește serverul
        server.setExecutor(null);
        if (emulatorService.start() == 0)
            server.start();

        System.out.println("Serverul rulează pe http://172.16.10.166:8080");
    }
}
