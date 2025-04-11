package Repository;

import domain.PendingDispatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryPendingDispatch implements IRepository<PendingDispatch, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryPendingDispatch(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public PendingDispatch save(PendingDispatch entity) {
        logger.traceEntry("Saving pending dispatch: {}", entity);
        String sql = "INSERT INTO pending_dispatches(source_city, source_county, target_city, target_county, quantity, timestamp, dispatched) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setBoolean(7, entity.isDispatched());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving pending dispatch: {}", entity, e);
            throw new RuntimeException("Failed to save pending dispatch", e);
        }

        logger.trace("Saved pending dispatch: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<PendingDispatch> findById(Integer id) {
        logger.traceEntry("Finding pending dispatch by ID: {}", id);
        String sql = "SELECT * FROM pending_dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PendingDispatch pd = new PendingDispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp"),
                        rs.getBoolean("dispatched")
                );
                logger.trace("Found pending dispatch: {}", pd);
                logger.traceExit("Exiting...");
                return Optional.of(pd);
            }

        } catch (SQLException e) {
            logger.error("Error finding pending dispatch by ID: {}", id, e);
            throw new RuntimeException("Failed to find pending dispatch", e);
        }

        logger.traceExit("No pending dispatch found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<PendingDispatch> findAll() {
        logger.traceEntry("Fetching all pending dispatches");
        List<PendingDispatch> list = new ArrayList<>();
        String sql = "SELECT * FROM pending_dispatches";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PendingDispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp"),
                        rs.getBoolean("dispatched")
                ));
            }

        } catch (SQLException e) {
            logger.error("Error fetching pending dispatches", e);
            throw new RuntimeException("Failed to fetch pending dispatches", e);
        }

        logger.trace("Fetched {} pending dispatches", list.size());
        logger.traceExit("Exiting...");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting pending dispatch with ID: {}", id);
        String sql = "DELETE FROM pending_dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting pending dispatch with ID: {}", id, e);
            throw new RuntimeException("Failed to delete pending dispatch", e);
        }

        logger.trace("Deleted pending dispatch with ID: {}", id);
        logger.traceExit("Exiting...");
    }

    @Override
    public PendingDispatch update(PendingDispatch entity) {
        logger.traceEntry("Updating pending dispatch: {}", entity);
        String sql = "UPDATE pending_dispatches SET source_city = ?, source_county = ?, target_city = ?, target_county = ?, quantity = ?, timestamp = ?, dispatched = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setBoolean(7, entity.isDispatched());
            stmt.setInt(8, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating pending dispatch: {}", entity, e);
            throw new RuntimeException("Failed to update pending dispatch", e);
        }

        logger.trace("Updated pending dispatch: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
