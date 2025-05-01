package Service;

import Networking.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmulatorService {
    private final EmulatorNetworking emulatorNetworking;
    private final EmulatorNetworkingAmbulances emulatorNetworkingAmbulances;
    private final EmulatorNetworkingPolice emulatorNetworkingPolice;
    private final EmulatorNetworkingFire emulatorNetworkingFire;
    private final EmulatorNetworkingRescue emulatorNetworkingRescue;
    private final EmulatorNetworkingUtility emulatorNetworkingUtility;
    private List<Map<String, Object>> emergenciesForFrontend;
    private List<Map<String, Object>> pendingEmergenciesForFrontend;
    private boolean finished;
    private boolean frontend;

    public EmulatorService(EmulatorNetworking emulatorNetworking, EmulatorNetworkingAmbulances emulatorNetworkingAmbulances, EmulatorNetworkingPolice emulatorNetworkingPolice, EmulatorNetworkingFire emulatorNetworkingFire, EmulatorNetworkingRescue emulatorNetworkingRescue, EmulatorNetworkingUtility emulatorNetworkingUtility, boolean frontend){
        this.emulatorNetworking = emulatorNetworking;
        this.emulatorNetworkingAmbulances = emulatorNetworkingAmbulances;
        this.emulatorNetworkingPolice = emulatorNetworkingPolice;
        this.emulatorNetworkingFire = emulatorNetworkingFire;
        this.emulatorNetworkingRescue = emulatorNetworkingRescue;
        this.emulatorNetworkingUtility = emulatorNetworkingUtility;
        emergenciesForFrontend = new ArrayList<>();
        pendingEmergenciesForFrontend = new ArrayList<>();
        finished = false;
        this.frontend = frontend;
        try {
            emulatorNetworking.resetSimulation("salut", 10000, 100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> getNextEmergencyForFrontend() {
        if (!emergenciesForFrontend.isEmpty()) {
            Map<String, Object> stringObjectMap = emergenciesForFrontend.remove(0);
            pendingEmergenciesForFrontend.add(stringObjectMap);
            return stringObjectMap;
        }

        if (emergenciesForFrontend.isEmpty() && pendingEmergenciesForFrontend.isEmpty() && frontend) {
            try {
                emulatorNetworking.stopSimulation();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void deleteFromPending(String targetCounty, String targetCity, int quantity, String type) {
        for (Map<String, Object> emergency : pendingEmergenciesForFrontend) {
            String county = (String) emergency.get("county");
            String city = (String) emergency.get("city");

            if (county.equals(targetCounty) && city.equals(targetCity)) {
                // Dacă avem un obiect valid, căutăm tipul de urgență în lista de requests
                List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");

                for (Map<String, Object> request : requests) {
                    // Verificăm tipul urgenței și cantitatea
                    String requestType = (String) request.get("Type");
                    int requestQuantity = (Integer) request.get("Quantity");

                    // Dacă tipul și cantitatea se potrivesc
                    if (requestType.equals(type) && requestQuantity >= quantity) {
                        // Scădem cantitatea
                        request.put("Quantity", requestQuantity - quantity);

                        // Dacă toate cantitățile sunt 0 pentru Medical, Police și Fire, ștergem obiectul
                        if (isAllRequestsQuantityZero(requests)) {
                            pendingEmergenciesForFrontend.remove(emergency);
                        }
                        return;  // Am terminat operațiunea
                    }
                }
            }
        }

        if (emergenciesForFrontend.isEmpty() && pendingEmergenciesForFrontend.isEmpty() && frontend) {
            try {
                emulatorNetworking.stopSimulation();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Verifică dacă toate cantitățile din lista de requests sunt 0
    private boolean isAllRequestsQuantityZero(List<Map<String, Object>> requests) {
        for (Map<String, Object> request : requests) {
            int quantity = (Integer) request.get("Quantity");
            if (quantity > 0) {
                return false;  // Dacă găsim o cantitate mai mare de 0, returnăm false
            }
        }
        return true;  // Dacă toate cantitățile sunt 0, returnăm true
    }



    public int start() throws InterruptedException {
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

        List<Map<String, Object>> rescue;
        try {
            rescue = emulatorNetworkingRescue.getAvailableRescue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        List<Map<String, Object>> utility;
        try {
            utility = emulatorNetworkingUtility.getAvailableUtility();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (true) {
            Map<String, Object> emergency = null;
            try {
                emergency = emulatorNetworking.getNextEmergency();
                if (emergency == null)
                    break;
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
            if (!emergenciesForFrontend.contains(emergency))
                dispatchRescue(emergency, rescue);
            if (!emergenciesForFrontend.contains(emergency))
                dispatchUtility(emergency, utility);

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        if (!frontend){
//            try {
//                emulatorNetworking.stopSimulation();
//            } catch (IOException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }


        return 0;
    }

    private void dispatchRescue(Map<String, Object> emergency, List<Map<String, Object>> rescue) {
        List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");
        if (requests == null || requests.isEmpty()) {
            System.out.println("No requests found for this emergency.");
            return;
        }

        int neededRescue = (int) requests.get(3).get("Quantity");
        if(neededRescue == 0)
            return;
        Double emergencyLat = getLatitudeFromMap(emergency);
        Double emergencyLon = getLongitudeFromMap(emergency);

        String targetCounty = (String) emergency.get("county");
        String targetCity = (String) emergency.get("city");

        rescue.sort((rescue1, rescue2) -> {
            double dist1 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(rescue1), getLongitudeFromMap(rescue1));
            double dist2 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(rescue2), getLongitudeFromMap(rescue2));

            int distanceComparison = Double.compare(dist1, dist2);

            if (distanceComparison == 0) {
                String city1 = (String) rescue1.get("city");
                String city2 = (String) rescue2.get("city");
                return city1.compareTo(city2);
            }

            return distanceComparison;
        });

        double closestDistance = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(rescue.get(0)), getLongitudeFromMap(rescue.get(0)));

        if (closestDistance > 0.6 && frontend) {
            emergenciesForFrontend.add(emergency);
            return;
        }

        int dispatched = 0;
        String sourceCounty = "";
        String sourceCity = "";

        for (Map<String, Object> rsc : rescue) {
            if (dispatched >= neededRescue) break;

            int availableRescue = (int) rsc.get("quantity");
            if (availableRescue > 0) {
                sourceCounty = (String) rsc.get("county");
                sourceCity = (String) rsc.get("city");

                int rescueToDispatch = Math.min(neededRescue - dispatched, availableRescue);
                System.out.println("Dispatching " + rescueToDispatch + " rescue from " + sourceCity + ", " + sourceCounty);

                dispatched += rescueToDispatch;

                rsc.put("quantity", availableRescue - rescueToDispatch);

                emulatorNetworkingRescue.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, rescueToDispatch);
            }
        }

        if (dispatched < neededRescue) {
            System.out.println("Warning: Not enough rescue available to fulfill the request.");
        }
    }

    private void dispatchUtility(Map<String, Object> emergency, List<Map<String, Object>> utility) {
        List<Map<String, Object>> requests = (List<Map<String, Object>>) emergency.get("requests");
        if (requests == null || requests.isEmpty()) {
            System.out.println("No requests found for this emergency.");
            return;
        }

        int neededUtility = (int) requests.get(4).get("Quantity");
        if(neededUtility == 0)
            return;
        Double emergencyLat = getLatitudeFromMap(emergency);
        Double emergencyLon = getLongitudeFromMap(emergency);

        String targetCounty = (String) emergency.get("county");
        String targetCity = (String) emergency.get("city");

        utility.sort((utility1, utility2) -> {
            double dist1 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(utility1), getLongitudeFromMap(utility1));
            double dist2 = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(utility2), getLongitudeFromMap(utility2));

            int distanceComparison = Double.compare(dist1, dist2);

            if (distanceComparison == 0) {
                String city1 = (String) utility1.get("city");
                String city2 = (String) utility2.get("city");
                return city1.compareTo(city2);
            }

            return distanceComparison;
        });

        double closestDistance = calculateDistance(emergencyLat, emergencyLon, getLatitudeFromMap(utility.get(0)), getLongitudeFromMap(utility.get(0)));

        if (closestDistance > 0.6 && frontend) {
            emergenciesForFrontend.add(emergency);
            return;
        }

        int dispatched = 0;
        String sourceCounty = "";
        String sourceCity = "";

        for (Map<String, Object> utl : utility) {
            if (dispatched >= neededUtility) break;

            int availableUtility = (int) utl.get("quantity");
            if (availableUtility > 0) {
                sourceCounty = (String) utl.get("county");
                sourceCity = (String) utl.get("city");

                int utilityToDispatch = Math.min(neededUtility - dispatched, availableUtility);
                System.out.println("Dispatching " + utilityToDispatch + " utility from " + sourceCity + ", " + sourceCounty);

                dispatched += utilityToDispatch;

                utl.put("quantity", availableUtility - utilityToDispatch);

                emulatorNetworkingUtility.sendDispatchNotification(sourceCounty, sourceCity, targetCounty, targetCity, utilityToDispatch);
            }
        }

        if (dispatched < neededUtility) {
            System.out.println("Warning: Not enough utility available to fulfill the request.");
        }
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

        if (closestDistance > 0.6 && frontend) {
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

        if (closestDistance > 0.6 && frontend) {
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

        if (closestDistance > 0.6 && frontend) {
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
