package repository;

import domain.Emergency;
import domain.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;
import java.util.Properties;

public class RepositoryEmergency implements IRepository<Emergency, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all emergencies");
        String sql = "DELETE FROM emergencies";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} emergency records", rows);
        } catch (SQLException e) {
            logger.error("Error deleting emergencies", e);
            throw new RuntimeException("Failed to delete all emergencies: " + e.getMessage(), e);
        }

        logger.traceExit("All emergencies deleted");
    }

    @Override
    public Emergency save(Emergency entity) {
        logger.traceEntry("Saving emergency: {}", entity);
        String sql = "INSERT INTO emergencies(city, county, latitude, longitude, quantity_ambulance, quantity_police, quantity_fire) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantities().getOrDefault(ServiceType.MEDICAL, 0));
            stmt.setInt(6, entity.getQuantities().getOrDefault(ServiceType.POLICE, 0));
            stmt.setInt(7, entity.getQuantities().getOrDefault(ServiceType.FIRE, 0));

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving emergency: {}", entity, e);
            throw new RuntimeException("Failed to save emergency", e);
        }

        logger.trace("Saved emergency: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Emergency> findById(Integer id) {
        logger.traceEntry("Finding emergency by ID: {}", id);
        String sql = "SELECT * FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<ServiceType, Integer> quantities = new HashMap<>();
                    quantities.put(ServiceType.MEDICAL, rs.getInt("quantity_ambulance"));
                    quantities.put(ServiceType.POLICE, rs.getInt("quantity_police"));
                    quantities.put(ServiceType.FIRE, rs.getInt("quantity_fire"));

                    Emergency emergency = new Emergency(
                            rs.getString("city"),
                            rs.getString("county"),
                            rs.getDouble("latitude"),
                            rs.getDouble("longitude"),
                            quantities
                    );
                    emergency.setId(id);

                    logger.trace("Found emergency: {}", emergency);
                    logger.traceExit();
                    return Optional.of(emergency);
                }
            }

        } catch (SQLException e) {
            logger.error("Error finding emergency by ID: {}", id, e);
            throw new RuntimeException("Failed to find emergency", e);
        }

        logger.traceExit("No emergency found for ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Emergency> findAll() {
        logger.traceEntry("Fetching all emergencies");
        List<Emergency> emergencies = new ArrayList<>();
        String sql = "SELECT * FROM emergencies";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<ServiceType, Integer> quantities = new HashMap<>();
                quantities.put(ServiceType.MEDICAL, rs.getInt("quantity_ambulance"));
                quantities.put(ServiceType.POLICE, rs.getInt("quantity_police"));
                quantities.put(ServiceType.FIRE, rs.getInt("quantity_fire"));

                Emergency emergency = new Emergency(
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        quantities
                );
                emergency.setId(rs.getInt("id"));
                emergencies.add(emergency);
            }

        } catch (SQLException e) {
            logger.error("Error fetching emergencies", e);
            throw new RuntimeException("Failed to fetch emergencies", e);
        }

        logger.trace("Fetched {} emergencies", emergencies.size());
        logger.traceExit("Exiting findAll()");
        return emergencies;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting emergency by ID: {}", id);
        String sql = "DELETE FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting emergency with ID: {}", id, e);
            throw new RuntimeException("Failed to delete emergency", e);
        }

        logger.trace("Deleted emergency with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Emergency update(Emergency entity) {
        logger.traceEntry("Updating emergency: {}", entity);
        String sql = "UPDATE emergencies SET county = ?, latitude = ?, longitude = ?, quantity_ambulance = ?, quantity_police = ?, quantity_fire = ? WHERE city = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCounty());
            stmt.setDouble(2, entity.getLatitude());
            stmt.setDouble(3, entity.getLongitude());
            stmt.setInt(4, entity.getQuantities().getOrDefault(ServiceType.MEDICAL, 0));
            stmt.setInt(5, entity.getQuantities().getOrDefault(ServiceType.POLICE, 0));
            stmt.setInt(6, entity.getQuantities().getOrDefault(ServiceType.FIRE, 0));
            stmt.setString(7, entity.getCity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating emergency: {}", entity, e);
            throw new RuntimeException("Failed to update emergency", e);
        }

        logger.trace("Updated emergency: {}", entity);
        logger.traceExit("Exiting update()");
        return entity;
    }
}