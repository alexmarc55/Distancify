package repository;

import domain.Utility;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryUtility implements IRepository<Utility, Integer> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryUtility(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
        deleteAll();
    }

    private void deleteAll() {
        logger.traceEntry("Deleting all utility units");
        String sql = "DELETE FROM utility";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rows = stmt.executeUpdate();
            logger.trace("Deleted {} utility units from table", rows);
        } catch (SQLException e) {
            logger.error("Error deleting all utility units", e);
            throw new RuntimeException("Failed to delete all utility units: " + e.getMessage(), e);
        }

        logger.traceExit("All utility units deleted");
    }

    @Override
    public Utility save(Utility entity) {
        logger.traceEntry("Saving utility unit: {}", entity);
        String sql = "INSERT INTO utility(county, city, latitude, longitude, quantity) VALUES (?, ?, ?, ?, ?)";

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
            logger.error("Error saving utility unit: {}", entity, e);
            throw new RuntimeException("Failed to save utility unit: " + e.getMessage(), e);
        }

        logger.trace("Saved utility unit: {}", entity);
        logger.traceExit("Exiting save()");
        return entity;
    }

    @Override
    public Optional<Utility> findById(Integer id) {
        logger.traceEntry("Finding utility unit by ID: {}", id);
        String sql = "SELECT * FROM utility WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utility utility = new Utility(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                utility.setId(rs.getInt("id"));
                logger.trace("Found utility unit: {}", utility);
                logger.traceExit("Exiting findById()");
                return Optional.of(utility);
            }

        } catch (SQLException e) {
            logger.error("Error finding utility unit with ID: {}", id, e);
            throw new RuntimeException("Failed to find utility unit: " + e.getMessage(), e);
        }

        logger.traceExit("No utility unit found with ID: {}", id);
        return Optional.empty();
    }

    @Override
    public List<Utility> findAll() {
        logger.traceEntry("Fetching all utility units");
        List<Utility> list = new ArrayList<>();
        String sql = "SELECT * FROM utility";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Utility utility = new Utility(
                        rs.getString("county"),
                        rs.getString("city"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity")
                );
                utility.setId(rs.getInt("id"));
                list.add(utility);
            }

        } catch (SQLException e) {
            logger.error("Error fetching utility units", e);
            throw new RuntimeException("Failed to fetch utility units: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} utility units", list.size());
        logger.traceExit("Exiting findAll()");
        return list;
    }

    @Override
    public void deleteById(Integer id) {
        logger.traceEntry("Deleting utility unit with ID: {}", id);
        String sql = "DELETE FROM utility WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting utility unit with ID: {}", id, e);
            throw new RuntimeException("Failed to delete utility unit: " + e.getMessage(), e);
        }

        logger.trace("Deleted utility unit with ID: {}", id);
        logger.traceExit("Exiting deleteById()");
    }

    @Override
    public Utility update(Utility entity) {
        logger.traceEntry("Updating utility unit: {}", entity);
        String sql = "UPDATE utility SET county = ?, city = ?, latitude = ?, longitude = ?, quantity = ? WHERE id = ?";

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
            logger.error("Error updating utility unit: {}", entity, e);
            throw new RuntimeException("Failed to update utility unit: " + e.getMessage(), e);
        }

        logger.trace("Updated utility unit: {}", entity);
        logger.traceExit("Exiting update()");
        return entity;
    }
}