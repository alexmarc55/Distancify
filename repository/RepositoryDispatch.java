package Repository;

import domain.Dispatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryDispatch implements IRepository<Dispatch, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryDispatch(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Dispatch save(Dispatch entity) {
        logger.traceEntry("Saving dispatch: {}", entity);
        String sql = "INSERT INTO dispatches(source_city, source_county, target_city, target_county, quantity, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving dispatch: {}", entity, e);
            throw new RuntimeException("Failed to save dispatch: " + e.getMessage(), e);
        }

        logger.trace("Saved dispatch: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<Dispatch> findById(Integer id) {
        logger.traceEntry("Finding dispatch by ID: {}", id);
        String sql = "SELECT * FROM dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dispatch dispatch = new Dispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                logger.trace("Found dispatch: {}", dispatch);
                logger.traceExit("Exiting findById()");
                return Optional.of(dispatch);
            }

        } catch (SQLException e) {
            logger.error("Error finding dispatch with ID: {}", id, e);
            throw new RuntimeException("Failed to find dispatch: " + e.getMessage(), e);
        }

        logger.traceExit("No dispatch found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Dispatch> findAll() {
        logger.traceEntry("Fetching all dispatches");
        List<Dispatch> dispatches = new ArrayList<>();
        String sql = "SELECT * FROM dispatches";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Dispatch dispatch = new Dispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                dispatches.add(dispatch);
            }

        } catch (SQLException e) {
            logger.error("Error fetching dispatches", e);
            throw new RuntimeException("Failed to fetch dispatches: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} dispatches", dispatches.size());
        logger.traceExit("Exiting...");
        return dispatches;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting dispatch with ID: {}", id);
        String sql = "DELETE FROM dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting dispatch with ID: {}", id, e);
            throw new RuntimeException("Failed to delete dispatch: " + e.getMessage(), e);
        }

        logger.trace("Deleted dispatch with ID: {}", id);
        logger.traceExit("Exiting...");
    }

    @Override
    public Dispatch update(Dispatch entity) {
        logger.traceEntry("Updating dispatch: {}", entity);
        String sql = "UPDATE dispatches SET source_city = ?, source_county = ?, target_city = ?, target_county = ?, quantity = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setInt(7, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating dispatch: {}", entity, e);
            throw new RuntimeException("Failed to update dispatch: " + e.getMessage(), e);
        }

        logger.trace("Updated dispatch: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
