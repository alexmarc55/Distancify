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

import Networking.EmulatorNetworking;
import Networking.FrontendNetworking;
import Service.EmulatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        EmulatorService emulatorService = new EmulatorService();

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

        // Pornește serverul
        server.setExecutor(null);
        if (emulatorService.start() == 0)
            server.start();

        System.out.println("Serverul rulează pe http://172.16.10.166:8080");
    }
}
