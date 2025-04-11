package Repository;

import domain.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryLocation implements IRepository<Location, String> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryLocation(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Location save(Location entity) {
        logger.traceEntry("Saving location: {}", entity);
        String sql = "INSERT INTO locations(name, county, latitude, longitude) VALUES (?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error saving location: {}", entity, e);
            throw new RuntimeException("Failed to save location: " + e.getMessage(), e);
        }

        logger.trace("Saved location: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<Location> findById(String name) {
        logger.traceEntry("Finding location by name: {}", name);
        String sql = "SELECT * FROM locations WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Location location = new Location(
                        rs.getString("name"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );
                logger.trace("Found location: {}", location);
                logger.traceExit("Exiting...");
                return Optional.of(location);
            }

        } catch (SQLException e) {
            logger.error("Error finding location by name: {}", name, e);
            throw new RuntimeException("Failed to find location: " + e.getMessage(), e);
        }

        logger.traceExit("No location found with name: {}", name);
        return Optional.empty();
    }

    @Override
    public List<Location> findAll() {
        logger.traceEntry("Fetching all locations");
        List<Location> locations = new ArrayList<>();
        String sql = "SELECT * FROM locations";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Location location = new Location(
                        rs.getString("name"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude")
                );
                locations.add(location);
            }

        } catch (SQLException e) {
            logger.error("Error fetching locations", e);
            throw new RuntimeException("Failed to fetch locations: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} locations", locations.size());
        logger.traceExit("Exiting...");
        return locations;
    }

    @Override
    public void deleteById(String name) {
        logger.traceEntry("Deleting location with name: {}", name);
        String sql = "DELETE FROM locations WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting location with name: {}", name, e);
            throw new RuntimeException("Failed to delete location: " + e.getMessage(), e);
        }

        logger.trace("Deleted location with name: {}", name);
        logger.traceExit("Exiting...");
    }

    @Override
    public Location update(Location entity) {
        logger.traceEntry("Updating location: {}", entity);
        String sql = "UPDATE locations SET county = ?, latitude = ?, longitude = ? WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCounty());
            stmt.setDouble(2, entity.getLatitude());
            stmt.setDouble(3, entity.getLongitude());
            stmt.setString(4, entity.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating location: {}", entity, e);
            throw new RuntimeException("Failed to update location: " + e.getMessage(), e);
        }

        logger.trace("Updated location: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
