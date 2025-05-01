package Service;

import Networking.EmulatorNetworking;
import domain.Rescue;
import domain.Utility;
import repository.RepositoryUtility;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class UtilityService {
    private final RepositoryUtility repositoryUtility;
    private final EmulatorNetworking emulatorNetworking;

    public UtilityService(RepositoryUtility repositoryUtility, EmulatorNetworking emulatorNetworking) {
        this.repositoryUtility = repositoryUtility;
        this.emulatorNetworking = emulatorNetworking;
        init();
    }

    private void init() {
        List<Map<String, Object>> utilityUnits;

        try {
            utilityUnits = emulatorNetworking.getUtility(); // Ensure this method exists in EmulatorNetworking
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch utility unit data from emulator", e);
        }

        for (Map<String, Object> unit : utilityUnits) {
            addUtility(unit);
        }
    }

    public void addUtility(Map<String, Object> utility) {
        try {
            String county = (String) utility.get("county");
            String city = (String) utility.get("city");
            Object latitude = utility.get("lat");
            Object longitude = utility.get("long");
            Object quantity = utility.get("quantity");

            // Check if any required field is null
            if (county == null || city == null || latitude == null || longitude == null || quantity == null) {
                return;
            }

            // Convert values to appropriate types, assuming they are valid numbers
            double lat = ((Number) latitude).doubleValue();
            double lng = ((Number) longitude).doubleValue();
            int qty = ((Number) quantity).intValue();

            Utility rescueUnit = new Utility(county, city, lat, lng, qty);
            repositoryUtility.save(rescueUnit);
        } catch (Exception e) {
            throw new RuntimeException("Invalid rescue unit data: " + utility, e);
        }
    }
}