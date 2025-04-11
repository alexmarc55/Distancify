package Repository;

import domain.Emergency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryEmergency implements IRepository<Emergency, Integer> {

    private final JdbcUtils jdbcUtils;

    public RepositoryEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Emergency save(Emergency entity) {
        String sql = "INSERT INTO emergencies(city, county, latitude, longitude, quantity, resolved, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setBoolean(6, entity.isResolved());
            stmt.setTimestamp(7, entity.getTimestamp());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save emergency: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<Emergency> findById(Integer id) {
        String sql = "SELECT * FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Emergency emergency = new Emergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getBoolean("resolved"),
                        rs.getTimestamp("timestamp")
                );
                return Optional.of(emergency);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find emergency: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Emergency> findAll() {
        List<Emergency> emergencies = new ArrayList<>();
        String sql = "SELECT * FROM emergencies";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Emergency emergency = new Emergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getBoolean("resolved"),
                        rs.getTimestamp("timestamp")
                );
                emergencies.add(emergency);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch emergencies: " + e.getMessage(), e);
        }

        return emergencies;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete emergency: " + e.getMessage(), e);
        }
    }

    @Override
    public Emergency update(Emergency entity) {
        String sql = "UPDATE emergencies SET city = ?, county = ?, latitude = ?, longitude = ?, quantity = ?, resolved = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setBoolean(6, entity.isResolved());
            stmt.setTimestamp(7, entity.getTimestamp());
            stmt.setInt(8, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update emergency: " + e.getMessage(), e);
        }

        return entity;
    }
}
