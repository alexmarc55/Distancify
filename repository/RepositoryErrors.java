package Repository;

import domain.ErrorLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryErrors implements IRepository<ErrorLog, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryErrors(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public ErrorLog save(ErrorLog entity) {
        logger.traceEntry("Saving error log: {}", entity);
        String sql = "INSERT INTO errors(missed, over_dispatched, created_at) VALUES (?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, entity.getMissed());
            stmt.setInt(2, entity.getOverDispatched());
            stmt.setTimestamp(3, entity.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving error log: {}", entity, e);
            throw new RuntimeException("Failed to save error log: " + e.getMessage(), e);
        }

        logger.trace("Saved error log: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<ErrorLog> findById(Integer id) {
        logger.traceEntry("Finding error log by ID: {}", id);
        String sql = "SELECT * FROM errors WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ErrorLog error = new ErrorLog(
                        rs.getInt("id"),
                        rs.getInt("missed"),
                        rs.getInt("over_dispatched"),
                        rs.getTimestamp("created_at")
                );
                logger.trace("Found error log: {}", error);
                logger.traceExit("Exiting...");
                return Optional.of(error);
            }

        } catch (SQLException e) {
            logger.error("Error finding error log with ID: {}", id, e);
            throw new RuntimeException("Failed to find error log: " + e.getMessage(), e);
        }

        logger.traceExit("No error log found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<ErrorLog> findAll() {
        logger.traceEntry("Fetching all error logs");
        List<ErrorLog> errors = new ArrayList<>();
        String sql = "SELECT * FROM errors";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ErrorLog error = new ErrorLog(
                        rs.getInt("id"),
                        rs.getInt("missed"),
                        rs.getInt("over_dispatched"),
                        rs.getTimestamp("created_at")
                );
                errors.add(error);
            }

        } catch (SQLException e) {
            logger.error("Error fetching error logs", e);
            throw new RuntimeException("Failed to fetch error logs: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} error logs", errors.size());
        logger.traceExit("Exiting...");
        return errors;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting error log with ID: {}", id);
        String sql = "DELETE FROM errors WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting error log with ID: {}", id, e);
            throw new RuntimeException("Failed to delete error log: " + e.getMessage(), e);
        }

        logger.trace("Deleted error log with ID: {}", id);
        logger.traceExit("Exiting...");
    }

    @Override
    public ErrorLog update(ErrorLog entity) {
        logger.traceEntry("Updating error log: {}", entity);
        String sql = "UPDATE errors SET missed = ?, over_dispatched = ?, created_at = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entity.getMissed());
            stmt.setInt(2, entity.getOverDispatched());
            stmt.setTimestamp(3, entity.getCreatedAt());
            stmt.setInt(4, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating error log: {}", entity, e);
            throw new RuntimeException("Failed to update error log: " + e.getMessage(), e);
        }

        logger.trace("Updated error log: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
