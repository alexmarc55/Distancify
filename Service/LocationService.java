package Service;

import Networking.EmulatorNetworking;
import domain.Location;
import repository.RepositoryLocation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LocationService {
    private final RepositoryLocation locationRepository;
    private final EmulatorNetworking emulatorNetworking;

    public LocationService(RepositoryLocation locationRepository, EmulatorNetworking emulatorNetworking) {
        this.locationRepository = locationRepository;
        this.emulatorNetworking = emulatorNetworking;
        init();
    }

    private void init(){
        List<Map<String, Object>> locations = null;
        try {
            locations = emulatorNetworking.getLocations();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Map<String, Object> location : locations){
            addLocation(location);
        }
    }

    public void addLocation(Map<String, Object> location) {
        try {
            String name = (String) location.get("name");
            String county = (String) location.get("county");
            double latitude = ((Number) location.get("lat")).doubleValue();
            double longitude = ((Number) location.get("long")).doubleValue();

            Location loc = new Location(name, county, latitude, longitude);
            locationRepository.save(loc);
        } catch (Exception e) {
            throw new RuntimeException("Invalid location data: " + location, e);
        }
    }
}