package Repository;

import domain.Ambulance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryAmbulance implements IRepository<Ambulance, String> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryAmbulance(Properties properties) {
        this.jdbcUtils = new JdbcUtils(properties);
    }

    @Override
    public Ambulance save(Ambulance ambulance) {
        logger.traceEntry("Saving ambulance: {}", ambulance);
        String sql = "INSERT INTO ambulances(location, quantity) VALUES (?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ambulance.getLocation());
            stmt.setInt(2, ambulance.getQuantity());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error saving ambulance: {}", ambulance, e);
            throw new RuntimeException("Failed to save ambulance: " + e.getMessage(), e);
        }
        logger.trace("Saved ambulance: {}", ambulance)
        logger.traceExit("Exiting...");
        return ambulance;
    }

    @Override
    public Optional<Ambulance> findById(String location) {
        logger.traceEntry("Finding ambulance by location: {}", location);
        String sql = "SELECT * FROM ambulances WHERE location = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Ambulance ambulance = new Ambulance(
                        rs.getString("location"),
                        rs.getInt("quantity")
                );
                logger.trace("Found ambulance: {}", ambulance);
                logger.traceExit("Exiting...");
                return Optional.of(ambulance);
            }

        } catch (SQLException e) {
            logger.error("Error finding ambulance at location: {}", location, e);
            throw new RuntimeException("Failed to find ambulance: " + e.getMessage(), e);
        }

        logger.traceExit("No ambulance found at location: {}", location);
        return Optional.empty();
    }

    @Override
    public List<Ambulance> findAll() {
        logger.traceEntry("Fetching all ambulances");
        List<Ambulance> ambulances = new ArrayList<>();
        String sql = "SELECT * FROM ambulances";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ambulance ambulance = new Ambulance(
                        rs.getString("location"),
                        rs.getInt("quantity")
                );
                ambulances.add(ambulance);
            }

        } catch (SQLException e) {
            logger.error("Error fetching ambulances", e);
            throw new RuntimeException("Failed to fetch ambulances: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} ambulances", ambulances.size());
        logger.traceExit("Exiting...");
        return ambulances;
    }

    @Override
    public void deleteById(String location) {
        logger.traceEntry("Deleting ambulance at location: {}", location);
        String sql = "DELETE FROM ambulances WHERE location = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting ambulance at location: {}", location, e);
            throw new RuntimeException("Failed to delete ambulance: " + e.getMessage(), e);
        }

        logger.trace("Deleted ambulance at location: {}", location);
        logger.traceExit("Exiting...");
    }

    @Override
    public Ambulance update(Ambulance ambulance) {
        logger.traceEntry("Updating ambulance: {}", ambulance);
        String sql = "UPDATE ambulances SET quantity = ? WHERE location = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ambulance.getQuantity());
            stmt.setString(2, ambulance.getLocation());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating ambulance: {}", ambulance, e);
            throw new RuntimeException("Failed to update ambulance: " + e.getMessage(), e);
        }

        logger.trace("Updated ambulance: {}", ambulance);
        logger.traceExit("Exiting...");
        return ambulance;
    }
}
