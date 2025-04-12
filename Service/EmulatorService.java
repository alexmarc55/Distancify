package Service;

import Networking.EmulatorNetworking;
import Networking.EmulatorNetworkingAmbulances;
import Networking.EmulatorNetworkingFire;
import Networking.EmulatorNetworkingPolice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmulatorService {
    EmulatorNetworking emulatorNetworking = new EmulatorNetworking();
    EmulatorNetworkingAmbulances emulatorNetworkingAmbulances = new EmulatorNetworkingAmbulances();
    EmulatorNetworkingPolice emulatorNetworkingPolice = new EmulatorNetworkingPolice();
    EmulatorNetworkingFire emulatorNetworkingFire = new EmulatorNetworkingFire();
    List<Map<String, Object>> emergenciesForFrontend = new ArrayList<>();
    boolean finished;

    public EmulatorService(){
        finished = false;
    }


    public Map<String, Object> getNextEmergencyForFrontend() {
        if (!emergenciesForFrontend.isEmpty()) {
            return emergenciesForFrontend.remove(0); // scoate și returnează primul element
        }

        if (finished) {
            try {
                emulatorNetworking.stopSimulation();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public int start() throws InterruptedException {
        try {
            emulatorNetworking.resetSimulation("default", 500, 100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> ambulances;
        try {
            ambulances = emulatorNetworkingAmbulances.getAvailableAmbulances();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> fire;
        try {
            fire = emulatorNetworkingFire.getAvailableFire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> police;
        try {
            police = emulatorNetworkingPolice.getAvailablePolice();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < 500; i++) {
            Map<String, Object> emergency = null;
            try {
                emergency = emulatorNetworking.getNextEmergency();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (emergency == null)
                break;

            dispatchAmbulances(emergency, ambulances);
            if (!emergenciesForFrontend.contains(emergency))
                dispatchPolice(emergency, police);
            if (!emergenciesForFrontend.contains(emergency))
                dispatchFire(emergency, fire);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        finished = true;

        return 0;
    }

    public void dispatchAmbulances(Map<String, Object> emergency, List<Map<String, Object>> ambulances) {

        List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");
        if (requests == null || requests.isEmpty()) {
            System.out.println("No requests found for this emergency.");
            return;
        }

        int neededAmbulances = (int) requests.get(0).get("Quantity");
        if(neededAmbulances == 0)
            return;
        Double emergencyLat = getLatitudeFromMap(emergency);
        Double emergencyLon = getLongitudeFromMap(emergency);

        String targetCounty = (String) emergency.get("county");
        String targetCity = (String) emergency.get("city");

        ambulances.sort((ambulance1, ambulance2) -> {
            double dist1 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(ambulance1), getLongitudeFromMap(ambulance1));
            double dist2 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(ambulance2), getLongitudeFromMap(ambulance2));

            int distanceComparison = Double.compare(dist1, dist2);

            if (distanceComparison == 0) {
                String city1 = (String) ambulance1.get("city");
                String city2 = (String) ambulance2.get("city");
                return city1.compareTo(city2);
            }

            return distanceComparison;
        });

        double closestDistance = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(ambulances.get(0)), getLongitudeFromMap(ambulances.get(0)));

        if (closestDistance > 0.4) {
            emergenciesForFrontend.add(emergency);
            return;
        }

        int dispatched = 0;
        String sourceCounty = "";
        String sourceCity = "";

        for (Map<String, Object> ambulance : ambulances) {
            if (dispatched >= neededAmbulances) break;

            int availableAmbulances = (int) ambulance.get("quantity");
            if (availableAmbulances > 0) {
                sourceCounty = (String) ambulance.get("county");
                sourceCity = (String) ambulance.get("city");

                int ambulancesToDispatch = Math.min(neededAmbulances - dispatched, availableAmbulances);
                System.out.println("Dispatching " + ambulancesToDispatch + " ambulances from " + sourceCity + ", " + sourceCounty);

                dispatched += ambulancesToDispatch;

                ambulance.put("quantity", availableAmbulances - ambulancesToDispatch);

                emulatorNetworkingAmbulances.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, ambulancesToDispatch);
            }
        }

        if (dispatched < neededAmbulances) {
            System.out.println("Warning: Not enough ambulances available to fulfill the request.");
        }
    }

    public void dispatchPolice(Map<String, Object> emergency, List<Map<String, Object>> policeOffice) {

        List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");
        if (requests == null || requests.isEmpty()) {
            System.out.println("No requests found for this emergency.");
            return;
        }

        int neededPolice = (int) requests.get(2).get("Quantity");
        if (neededPolice == 0)
            return;
        Double emergencyLat = getLatitudeFromMap(emergency);
        Double emergencyLon = getLongitudeFromMap(emergency);

        String targetCounty = (String) emergency.get("county");
        String targetCity = (String) emergency.get("city");

        policeOffice.sort((police1, police2) -> {
            double dist1 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(police1), getLongitudeFromMap(police1));
            double dist2 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(police2), getLongitudeFromMap(police2));

            int distanceComparison = Double.compare(dist1, dist2);

            if (distanceComparison == 0) {
                String city1 = (String) police1.get("city");
                String city2 = (String) police2.get("city");
                return city1.compareTo(city2);
            }

            return distanceComparison;
        });

        double closestDistance = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(policeOffice.get(0)), getLongitudeFromMap(policeOffice.get(0)));

        if (closestDistance > 0.6) {
            emergenciesForFrontend.add(emergency);
            return;
        }

        int dispatched = 0;
        String sourceCounty = "";
        String sourceCity = "";

        for (Map<String, Object> police : policeOffice) {
            if (dispatched >= neededPolice) break;

            int availablePolice = (int) police.get("quantity");
            if (availablePolice > 0) {
                sourceCounty = (String) police.get("county");
                sourceCity = (String) police.get("city");

                int policeToDispatch = Math.min(neededPolice - dispatched, availablePolice);
                System.out.println("Dispatching " + policeToDispatch + " police from " + sourceCity + ", " + sourceCounty);

                dispatched += policeToDispatch;

                police.put("quantity", availablePolice - policeToDispatch);

                emulatorNetworkingPolice.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, policeToDispatch);
            }
        }

        if (dispatched < neededPolice) {
            System.out.println("Warning: Not enough police available to fulfill the request.");
        }
    }

    public void dispatchFire(Map<String, Object> emergency, List<Map<String, Object>> fireOffice) {

        List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");
        if (requests == null || requests.isEmpty()) {
            System.out.println("No requests found for this emergency.");
            return;
        }

        int neededFire = (int) requests.get(1).get("Quantity");
        if (neededFire == 0)
            return;

        Double emergencyLat = getLatitudeFromMap(emergency);
        Double emergencyLon = getLongitudeFromMap(emergency);

        String targetCounty = (String) emergency.get("county");
        String targetCity = (String) emergency.get("city");

        fireOffice.sort((fire1, fire2) -> {
            double dist1 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(fire1), getLongitudeFromMap(fire1));
            double dist2 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(fire2), getLongitudeFromMap(fire2));

            int distanceComparison = Double.compare(dist1, dist2);

            if (distanceComparison == 0) {
                String city1 = (String) fire1.get("city");
                String city2 = (String) fire2.get("city");
                return city1.compareTo(city2);
            }

            return distanceComparison;
        });

        double closestDistance = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(fireOffice.get(0)), getLongitudeFromMap(fireOffice.get(0)));

        if (closestDistance > 0.6) {
            emergenciesForFrontend.add(emergency);
            return;
        }

        int dispatched = 0;
        String sourceCounty = "";
        String sourceCity = "";

        for (Map<String, Object> fire : fireOffice) {
            if (dispatched >= neededFire) break;

            int availableFire = (int) fire.get("quantity");
            if (availableFire > 0) {
                sourceCounty = (String) fire.get("county");
                sourceCity = (String) fire.get("city");

                int fireToDispatch = Math.min(neededFire - dispatched, availableFire);
                System.out.println("Dispatching " + fireToDispatch + " fire from " + sourceCity + ", " + sourceCounty);

                dispatched += fireToDispatch;

                fire.put("quantity", availableFire - fireToDispatch);

                emulatorNetworkingFire.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, fireToDispatch);
            }
        }

        if (dispatched < neededFire) {
            System.out.println("Warning: Not enough police available to fulfill the request.");
        }
    }


    private static Double getLatitudeFromMap(Map<String, Object> map) {
        Object latitude = map.get("latitude");
        if (latitude instanceof Double) {
            return (Double) latitude;
        } else if (latitude instanceof Integer) {
            return ((Integer) latitude).doubleValue();  // Convert Integer to Double
        }
        return null;
    }

    private static Double getLongitudeFromMap(Map<String, Object> map) {
        Object longitude = map.get("longitude");
        if (longitude instanceof Double) {
            return (Double) longitude;
        } else if (longitude instanceof Integer) {
            return ((Integer) longitude).doubleValue();  // Convert Integer to Double
        }
        return null; // or throw an exception if you expect this value to always exist
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        return Math.sqrt(Math.pow(lat2 - lat1, 2) + Math.pow(lon2 - lon1, 2));
    }
}
