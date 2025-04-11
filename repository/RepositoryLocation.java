package Repository;

import domain.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryLocation implements IRepository<Location, String> {

    private final JdbcUtils jdbcUtils;

    public RepositoryLocation(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Location save(Location entity) {
        String sql = "INSERT INTO locations(name, county, latitude, longitude) VALUES (?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save location: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<Location> findById(String name) {
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
                return Optional.of(location);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find location: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Location> findAll() {
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
            throw new RuntimeException("Failed to fetch locations: " + e.getMessage(), e);
        }

        return locations;
    }

    @Override
    public void deleteById(String name) {
        String sql = "DELETE FROM locations WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete location: " + e.getMessage(), e);
        }
    }

    @Override
    public Location update(Location entity) {
        String sql = "UPDATE locations SET county = ?, latitude = ?, longitude = ? WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCounty());
            stmt.setDouble(2, entity.getLatitude());
            stmt.setDouble(3, entity.getLongitude());
            stmt.setString(4, entity.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update location: " + e.getMessage(), e);
        }

        return entity;
    }
}
