package repository;

import domain.Rescue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryRescue implements IRepository<Rescue, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryRescue(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all Rescues");
        String sql = "DELETE FROM rescue";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} Rescue from table", rows);
        } catch (SQLException e) {
            logger.error("Error deleting all Rescue", e);
            throw new RuntimeException("Failed to delete all Rescue: " + e.getMessage(), e);
        }

        logger.traceExit("All Rescue deleted");
    }

    @Override
    public Rescue save(Rescue entity) {
        logger.traceEntry("Saving Rescue unit: {}", entity);
        String sql = "INSERT INTO rescue(county, city, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCounty());
            stmt.setString(2, entity.getCity());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            logger.error("Error saving Rescue unit: {}", entity, e);
            throw new RuntimeException("Failed to save Rescue unit: " + e.getMessage(), e);
        }

        logger.trace("Saved Rescue unit: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Rescue> findById(Integer id) {
        logger.traceEntry("Finding police unit by ID: {}", id);
        String sql = "SELECT * FROM rescue WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Rescue rescue = new Rescue(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                rescue.setId(rs.getInt("id"));
                logger.trace("Found police unit: {}", rescue);
                logger.traceExit("Exiting findById()");
                return Optional.of(rescue);
            }

        } catch (SQLException e) {
            logger.error("Error finding police unit with ID: {}", id, e);
            throw new RuntimeException("Failed to find police unit: " + e.getMessage(), e);
        }

        logger.traceExit("No police unit found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Rescue> findAll() {
        logger.traceEntry("Fetching all police units");
        List<Rescue> list = new ArrayList<>();
        String sql = "SELECT * FROM rescue";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Rescue rescue = new Rescue(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                rescue.setId(rs.getInt("id"));
                list.add(rescue);
            }

        } catch (SQLException e) {
            logger.error("Error fetching police units", e);
            throw new RuntimeException("Failed to fetch Rescue units: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} Rescue units", list.size());
        logger.traceExit("Exiting findAll()");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting Rescue unit with ID: {}", id);
        String sql = "DELETE FROM rescue WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting Rescue unit with ID: {}", id, e);
            throw new RuntimeException("Failed to delete Rescue unit: " + e.getMessage(), e);
        }

        logger.trace("Deleted Rescue unit with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Rescue update(Rescue entity) {
        logger.traceEntry("Updating Rescue unit: {}", entity);
        String sql = "UPDATE rescue SET county = ?, city = ?, latitude = ?, longitude = ?, quantity = ? WHERE id = ?";

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
            logger.error("Error updating Rescue unit: {}", entity, e);
            throw new RuntimeException("Failed to update Rescue unit: " + e.getMessage(), e);
        }

        logger.trace("Updated Rescue unit: {}", entity);
        logger.traceExit("Exiting update()");
        return entity;
    }
}