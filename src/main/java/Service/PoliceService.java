package Service;

import Networking.EmulatorNetworking;
import domain.Police;
import repository.RepositoryPolice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PoliceService {
    private final RepositoryPolice repositoryPolice;
    private final EmulatorNetworking emulatorNetworking;

    public PoliceService(RepositoryPolice repositoryPolice, EmulatorNetworking emulatorNetworking) {
        this.repositoryPolice = repositoryPolice;
        this.emulatorNetworking = emulatorNetworking;
        init();
    }

    private void init() {
        List<Map<String, Object>> policeUnits;

        try {
            policeUnits = emulatorNetworking.getPolice(); // Make sure this exists in EmulatorNetworking
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to fetch police unit data from emulator", e);
        }

        for (Map<String, Object> unit : policeUnits) {
            addPolice(unit);
        }
    }

    public void addPolice(Map<String, Object> police) {
        try {
            String county = (String) police.get("county");
            String city = (String) police.get("city");
            Object latitude = police.get("lat");
            Object longitude = police.get("long");
            Object quantity = police.get("quantity");

            // Check if any required field is null
            if (county == null || city == null || latitude == null || longitude == null || quantity == null) {
                return;
            }

            // Convert values to appropriate types, assuming they are valid numbers
            double lat = ((Number) latitude).doubleValue();
            double lng = ((Number) longitude).doubleValue();
            int qty = ((Number) quantity).intValue();

            // Create a new Police unit and save it
            Police policeUnit = new Police(county, city, lat, lng, qty);
            repositoryPolice.save(policeUnit);
        } catch (Exception e) {
            throw new RuntimeException("Invalid police unit data: " + police, e);
        }
    }

}