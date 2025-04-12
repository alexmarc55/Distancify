package Service;

import Networking.EmulatorNetworking;
import domain.Ambulances;
import repository.RepositoryAmbulances;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AmbulanceService {
    private final RepositoryAmbulances repositoryAmbulances;
    private final EmulatorNetworking emulatorNetworking;

    public AmbulanceService(RepositoryAmbulances repositoryAmbulances, EmulatorNetworking emulatorNetworking) {
        this.repositoryAmbulances = repositoryAmbulances;
        this.emulatorNetworking = emulatorNetworking;
        init();
    }

    private void init() {
        List<Map<String, Object>> ambulances;

        try {
            ambulances = emulatorNetworking.getAmbulances();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch ambulance data from emulator", e);
        }

        for (Map<String, Object> ambulance : ambulances) {
            addAmbulance(ambulance);
        }
    }

    public void addAmbulance(Map<String, Object> ambulance) {
        try {
            String county = (String) ambulance.get("county");
            String city = (String) ambulance.get("city");
            double lat = ((Number) ambulance.get("latitude")).doubleValue();
            double lng = ((Number) ambulance.get("longitude")).doubleValue();
            int quantity = ((Number) ambulance.get("quantity")).intValue();

            Ambulances a = new Ambulances(county, city, lat, lng, quantity);

            repositoryAmbulances.save(a);
        } catch (Exception e) {
            throw new RuntimeException("Invalid ambulance data: " + ambulance, e);
        }
    }

}