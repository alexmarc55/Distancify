package repository;

import domain.Fire;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryFire implements IRepository<Fire, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryFire(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all fires");
        String sql = "DELETE FROM fire";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} fire(s) from table", rows);
        } catch (SQLException e) {
            logger.error("Error deleting all fires", e);
            throw new RuntimeException("Failed to delete all fires: " + e.getMessage(), e);
        }

        logger.traceExit("All fires deleted");
    }

    @Override
    public Fire save(Fire entity) {
        logger.traceEntry("Saving fire unit: {}", entity);
        String sql = "INSERT INTO fire(county, city, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCounty());
            stmt.setString(2, entity.getCity());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error saving fire unit: {}", entity, e);
            throw new RuntimeException("Failed to save fire unit: " + e.getMessage(), e);
        }

        logger.trace("Saved fire unit: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Fire> findById(Integer id) {
        logger.traceEntry("Finding fire unit by ID: {}", id);
        String sql = "SELECT * FROM fire WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Fire fire = new Fire(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                fire.setId(rs.getInt("id"));
                logger.trace("Found fire unit: {}", fire);
                logger.traceExit("Exiting findById()");
                return Optional.of(fire);
            }

        } catch (SQLException e) {
            logger.error("Error finding fire unit with ID: {}", id, e);
            throw new RuntimeException("Failed to find fire unit: " + e.getMessage(), e);
        }

        logger.traceExit("No fire unit found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Fire> findAll() {
        logger.traceEntry("Fetching all fire units");
        List<Fire> list = new ArrayList<>();
        String sql = "SELECT * FROM fire";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Fire fire = new Fire(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                fire.setId(rs.getInt("id"));
                list.add(fire);
            }

        } catch (SQLException e) {
            logger.error("Error fetching fire units", e);
            throw new RuntimeException("Failed to fetch fire units: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} fire units", list.size());
        logger.traceExit("Exiting findAll()");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting fire unit with ID: {}", id);
        String sql = "DELETE FROM fire WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting fire unit with ID: {}", id, e);
            throw new RuntimeException("Failed to delete fire unit: " + e.getMessage(), e);
        }

        logger.trace("Deleted fire unit with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Fire update(Fire entity) {
        logger.traceEntry("Updating fire unit: {}", entity);
        String sql = "UPDATE fire SET county = ?, city = ?, latitude = ?, longitude = ?, quantity = ? WHERE id = ?";

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
            logger.error("Error updating fire unit: {}", entity, e);
            throw new RuntimeException("Failed to update fire unit: " + e.getMessage(), e);
        }

        logger.trace("Updated fire unit: {}", entity);
        logger.traceExit("Exiting update()");
        return entity;
    }
}