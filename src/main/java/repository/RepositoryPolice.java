package repository;

import domain.Police;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryPolice implements IRepository<Police, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryPolice(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all locations");
        String sql = "DELETE FROM police";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} police from table", rows);
        } catch (SQLException e) {
            logger.error("Error deleting all police", e);
            throw new RuntimeException("Failed to delete all police: " + e.getMessage(), e);
        }

        logger.traceExit("All police deleted");
    }

    @Override
    public Police save(Police entity) {
        logger.traceEntry("Saving police unit: {}", entity);
        String sql = "INSERT INTO police(county, city, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCounty());
            stmt.setString(2, entity.getCity());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving police unit: {}", entity, e);
            throw new RuntimeException("Failed to save police unit: " + e.getMessage(), e);
        }

        logger.trace("Saved police unit: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Police> findById(Integer id) {
        logger.traceEntry("Finding police unit by ID: {}", id);
        String sql = "SELECT * FROM police WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Police police = new Police(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                police.setId(rs.getInt("id"));
                logger.trace("Found police unit: {}", police);
                logger.traceExit("Exiting findById()");
                return Optional.of(police);
            }

        } catch (SQLException e) {
            logger.error("Error finding police unit with ID: {}", id, e);
            throw new RuntimeException("Failed to find police unit: " + e.getMessage(), e);
        }

        logger.traceExit("No police unit found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Police> findAll() {
        logger.traceEntry("Fetching all police units");
        List<Police> list = new ArrayList<>();
        String sql = "SELECT * FROM police";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Police police = new Police(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                police.setId(rs.getInt("id"));
                list.add(police);
            }

        } catch (SQLException e) {
            logger.error("Error fetching police units", e);
            throw new RuntimeException("Failed to fetch police units: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} police units", list.size());
        logger.traceExit("Exiting findAll()");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting police unit with ID: {}", id);
        String sql = "DELETE FROM police WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting police unit with ID: {}", id, e);
            throw new RuntimeException("Failed to delete police unit: " + e.getMessage(), e);
        }

        logger.trace("Deleted police unit with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Police update(Police entity) {
        logger.traceEntry("Updating police unit: {}", entity);
        String sql = "UPDATE police SET county = ?, city = ?, latitude = ?, longitude = ?, quantity = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCounty());
            stmt.setString(2, entity.getCity());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setInt(6, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating police unit: {}", entity, e);
            throw new RuntimeException("Failed to update police unit: " + e.getMessage(), e);
        }

        logger.trace("Updated police unit: {}", entity);
        logger.traceExit("Exiting update()");
        return entity;
    }
}