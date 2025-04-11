package repository;

import domain.Emergency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryEmergency implements IRepository<Emergency, String> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Emergency save(Emergency entity) {
        logger.traceEntry("Saving emergency: {}", entity);
        String sql = "INSERT INTO emergencies(city, county, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error saving emergency: {}", entity, e);
            throw new RuntimeException("Failed to save emergency: " + e.getMessage(), e);
        }

        logger.trace("Saved emergency: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Emergency> findById(String city) {
        logger.traceEntry("Finding emergency by city: {}", city);
        String sql = "SELECT * FROM emergencies WHERE city = ? LIMIT 1"; // assumes city is unique or takes first

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Emergency emergency = new Emergency(
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                logger.trace("Found emergency: {}", emergency);
                logger.traceExit("Exiting findById()");
                return Optional.of(emergency);
            }

        } catch (SQLException e) {
            logger.error("Error finding emergency by city: {}", city, e);
            throw new RuntimeException("Failed to find emergency", e);
        }

        logger.traceExit("No emergency found for city: {}", city);
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
                Emergency emergency = new Emergency(
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                emergencies.add(emergency);
            }

        } catch (SQLException e) {
            logger.error("Error fetching emergencies", e);
            throw new RuntimeException("Failed to fetch emergencies: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} emergencies", emergencies.size());
        logger.traceExit("Exiting findAll()");
        return emergencies;
    }

    @Override
    public void deleteById(String city) {
        logger.traceEntry("Deleting emergency in city: {}", city);
        String sql = "DELETE FROM emergencies WHERE city = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, city);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting emergency in city: {}", city, e);
            throw new RuntimeException("Failed to delete emergency", e);
        }

        logger.trace("Deleted emergency in city: {}", city);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Emergency update(Emergency entity) {
        logger.traceEntry("Updating emergency: {}", entity);
        String sql = "UPDATE emergencies SET county = ?, latitude = ?, longitude = ?, quantity = ? WHERE city = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCounty());
            stmt.setDouble(2, entity.getLatitude());
            stmt.setDouble(3, entity.getLongitude());
            stmt.setInt(4, entity.getQuantity());
            stmt.setString(5, entity.getCity());

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
