package Service;

import Networking.EmulatorNetworking;
import domain.Rescue;
import repository.RepositoryRescue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RescueService {
    private final RepositoryRescue repositoryRescue;
    private final EmulatorNetworking emulatorNetworking;

    public RescueService(RepositoryRescue repositoryRescue, EmulatorNetworking emulatorNetworking) {
        this.repositoryRescue = repositoryRescue;
        this.emulatorNetworking = emulatorNetworking;
        init();
    }

    private void init() {
        List<Map<String, Object>> rescueUnits;

        try {
            rescueUnits = emulatorNetworking.getRescue(); // Ensure this method exists in EmulatorNetworking
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch rescue unit data from emulator", e);
        }

        for (Map<String, Object> unit : rescueUnits) {
            addRescue(unit);
        }
    }

    public void addRescue(Map<String, Object> rescue) {
        try {
            String county = (String) rescue.get("county");
            String city = (String) rescue.get("city");
            Object latitude = rescue.get("lat");
            Object longitude = rescue.get("long");
            Object quantity = rescue.get("quantity");

            // Check if any required field is null
            if (county == null || city == null || latitude == null || longitude == null || quantity == null) {
                return;
            }

            // Convert values to appropriate types, assuming they are valid numbers
            double lat = ((Number) latitude).doubleValue();
            double lng = ((Number) longitude).doubleValue();
            int qty = ((Number) quantity).intValue();

            Rescue rescueUnit = new Rescue(county, city, lat, lng, qty);
            repositoryRescue.save(rescueUnit);
        } catch (Exception e) {
            throw new RuntimeException("Invalid rescue unit data: " + rescue, e);
        }
    }
}