package Repository;

import domain.Emergency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryEmergency implements IRepository<Emergency, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Emergency save(Emergency entity) {
        logger.traceEntry("Saving emergency: {}", entity);
        String sql = "INSERT INTO emergencies(city, county, latitude, longitude, quantity, resolved, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setBoolean(6, entity.isResolved());
            stmt.setTimestamp(7, entity.getTimestamp());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving emergency: {}", entity, e);
            throw new RuntimeException("Failed to save emergency: " + e.getMessage(), e);
        }

        logger.trace("Saved emergency: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<Emergency> findById(Integer id) {
        logger.traceEntry("Finding emergency by ID: {}", id);
        String sql = "SELECT * FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Emergency emergency = new Emergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getBoolean("resolved"),
                        rs.getTimestamp("timestamp")
                );
                logger.trace("Found emergency: {}", emergency);
                logger.traceExit("Exiting...");
                return Optional.of(emergency);
            }

        } catch (SQLException e) {
            logger.error("Error finding emergency with ID: {}", id, e);
            throw new RuntimeException("Failed to find emergency: " + e.getMessage(), e);
        }

        logger.traceExit("No emergency found with ID: {}", id);
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
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getBoolean("resolved"),
                        rs.getTimestamp("timestamp")
                );
                emergencies.add(emergency);
            }

        } catch (SQLException e) {
            logger.error("Error fetching emergencies", e);
            throw new RuntimeException("Failed to fetch emergencies: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} emergencies", emergencies.size());
        logger.traceExit("Exiting...");
        return emergencies;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting emergency with ID: {}", id);
        String sql = "DELETE FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting emergency with ID: {}", id, e);
            throw new RuntimeException("Failed to delete emergency: " + e.getMessage(), e);
        }

        logger.trace("Deleted emergency with ID: {}", id);
        logger.traceExit("Exiting...");
    }

    @Override
    public Emergency update(Emergency entity) {
        logger.traceEntry("Updating emergency: {}", entity);
        String sql = "UPDATE emergencies SET city = ?, county = ?, latitude = ?, longitude = ?, quantity = ?, resolved = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setBoolean(6, entity.isResolved());
            stmt.setTimestamp(7, entity.getTimestamp());
            stmt.setInt(8, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating emergency: {}", entity, e);
            throw new RuntimeException("Failed to update emergency: " + e.getMessage(), e);
        }

        logger.trace("Updated emergency: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
