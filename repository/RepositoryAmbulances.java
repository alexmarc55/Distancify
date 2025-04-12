package repository;

import domain.Ambulances;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryAmbulances implements IRepository<Ambulances, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryAmbulances(Properties properties) {
        this.jdbcUtils = new JdbcUtils(properties);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all fires");
        String sql = "DELETE FROM ambulances";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} ambulances from table", rows);
        } catch (SQLException e) {
            logger.error("Error deleting all ambulances", e);
            throw new RuntimeException("Failed to delete all fires: " + e.getMessage(), e);
        }

        logger.traceExit("All ambulances deleted");
    }

    @Override
    public Ambulances save(Ambulances ambulance) {
        logger.traceEntry("Saving ambulance: {}", ambulance);
        String sql = "INSERT INTO ambulances(county, city, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ambulance.getCounty());
            stmt.setString(2, ambulance.getCity());
            stmt.setDouble(3, ambulance.getLatitude());
            stmt.setDouble(4, ambulance.getLongitude());
            stmt.setInt(5, ambulance.getQuantity());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving ambulance: {}", ambulance, e);
            throw new RuntimeException("Failed to save ambulance: " + e.getMessage(), e);
        }

        logger.trace("Saved ambulance: {}", ambulance);
        logger.traceExit("Exiting save()");
        return ambulance;
    }

    @Override
    public Optional<Ambulances> findById(Integer id) {
        logger.traceEntry("Finding ambulance by ID: {}", id);
        String sql = "SELECT * FROM ambulances WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Ambulances ambulance = new Ambulances(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                ambulance.setId(rs.getInt("id"));

                logger.trace("Found ambulance: {}", ambulance);
                logger.traceExit("Exiting findById()");
                return Optional.of(ambulance);
            }

        } catch (SQLException e) {
            logger.error("Error finding ambulance with ID: {}", id, e);
            throw new RuntimeException("Failed to find ambulance: " + e.getMessage(), e);
        }

        logger.traceExit("No ambulance found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Ambulances> findAll() {
        logger.traceEntry("Fetching all ambulances");
        List<Ambulances> ambulances = new ArrayList<>();
        String sql = "SELECT * FROM ambulances";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ambulances ambulance = new Ambulances(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                ambulance.setId(rs.getInt("id"));
                ambulances.add(ambulance);
            }

        } catch (SQLException e) {
            logger.error("Error fetching ambulances", e);
            throw new RuntimeException("Failed to fetch ambulances: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} ambulances", ambulances.size());
        logger.traceExit("Exiting findAll()");
        return ambulances;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting ambulance by ID: {}", id);
        String sql = "DELETE FROM ambulances WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting ambulance by ID: {}", id, e);
            throw new RuntimeException("Failed to delete ambulance: " + e.getMessage(), e);
        }

        logger.trace("Deleted ambulance with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Ambulances update(Ambulances ambulance) {
        logger.traceEntry("Updating ambulance: {}", ambulance);
        String sql = "UPDATE ambulances SET county = ?, city = ?, latitude = ?, longitude = ?, quantity = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ambulance.getCounty());
            stmt.setString(2, ambulance.getCity());
            stmt.setDouble(3, ambulance.getLatitude());
            stmt.setDouble(4, ambulance.getLongitude());
            stmt.setInt(5, ambulance.getQuantity());
            stmt.setInt(6, ambulance.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating ambulance: {}", ambulance, e);
            throw new RuntimeException("Failed to update ambulance: " + e.getMessage(), e);
        }

        logger.trace("Updated ambulance: {}", ambulance);
        logger.traceExit("Exiting update()");
        return ambulance;
    }
}
