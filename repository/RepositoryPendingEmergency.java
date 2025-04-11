package Repository;

import domain.PendingEmergency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryPendingEmergency implements IRepository<PendingEmergency, Integer> {

    private final JdbcUtils jdbcUtils;

    public RepositoryPendingEmergency(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public PendingEmergency save(PendingEmergency entity) {
        String sql = "INSERT INTO pending_emergencies(city, county, latitude, longitude, quantity, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());

            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save pending emergency", e);
        }

        return entity;
    }

    @Override
    public Optional<PendingEmergency> findById(Integer id) {
        String sql = "SELECT * FROM pending_emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new PendingEmergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find pending emergency", e);
        }

        return Optional.empty();
    }

    @Override
    public List<PendingEmergency> findAll() {
        List<PendingEmergency> list = new ArrayList<>();
        String sql = "SELECT * FROM pending_emergencies";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(new PendingEmergency(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("county"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch pending emergencies", e);
        }

        return list;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM pending_emergencies WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete pending emergency", e);
        }
    }

    @Override
    public PendingEmergency update(PendingEmergency entity) {
        String sql = "UPDATE pending_emergencies SET city = ?, county = ?, latitude = ?, longitude = ?, quantity = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getCity());
            stmt.setString(2, entity.getCounty());
            stmt.setDouble(3, entity.getLatitude());
            stmt.setDouble(4, entity.getLongitude());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setInt(7, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update pending emergency", e);
        }

        return entity;
    }
}
