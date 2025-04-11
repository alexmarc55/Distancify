package Repository;

import domain.PendingEmergency;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryPendingEmergency implements IRepository<PendingEmergency, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryPendingEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public PendingEmergency save(PendingEmergency entity) {
        logger.traceEntry("Saving pending emergency: {}", entity);
        String sql = "INSERT INTO pending_emergencies(city, county, latitude, longitude, quantity, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving pending emergency: {}", entity, e);
            throw new RuntimeException("Failed to save pending emergency", e);
        }

        logger.trace("Saved pending emergency: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<PendingEmergency> findById(Integer id) {
        logger.traceEntry("Finding pending emergency by ID: {}", id);
        String sql = "SELECT * FROM pending_emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PendingEmergency pe = new PendingEmergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                logger.trace("Found pending emergency: {}", pe);
                logger.traceExit("Exiting findById()");
                return Optional.of(pe);
            }

        } catch (SQLException e) {
            logger.error("Error finding pending emergency with ID: {}", id, e);
            throw new RuntimeException("Failed to find pending emergency", e);
        }

        logger.traceExit("No pending emergency found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<PendingEmergency> findAll() {
        logger.traceEntry("Fetching all pending emergencies");
        List<PendingEmergency> list = new ArrayList<>();
        String sql = "SELECT * FROM pending_emergencies";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PendingEmergency pe = new PendingEmergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                list.add(pe);
            }

        } catch (SQLException e) {
            logger.error("Error fetching pending emergencies", e);
            throw new RuntimeException("Failed to fetch pending emergencies", e);
        }

        logger.trace("Fetched {} pending emergencies", list.size());
        logger.traceExit("Exiting...");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting pending emergency with ID: {}", id);
        String sql = "DELETE FROM pending_emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting pending emergency with ID: {}", id, e);
            throw new RuntimeException("Failed to delete pending emergency", e);
        }

        logger.trace("Deleted pending emergency with ID: {}", id);
        logger.traceExit("Exiting...");
    }

    @Override
    public PendingEmergency update(PendingEmergency entity) {
        logger.traceEntry("Updating pending emergency: {}", entity);
        String sql = "UPDATE pending_emergencies SET city = ?, county = ?, latitude = ?, longitude = ?, quantity = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setInt(7, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating pending emergency: {}", entity, e);
            throw new RuntimeException("Failed to update pending emergency", e);
        }

        logger.trace("Updated pending emergency: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
