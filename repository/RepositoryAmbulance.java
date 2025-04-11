package Repository;

import domain.Ambulance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryAmbulance implements IRepository<Ambulance, String> {

    private final JdbcUtils jdbcUtils;

    public RepositoryAmbulance(Properties properties) {
        this.jdbcUtils = new JdbcUtils(properties);
    }

    @Override
    public Ambulance save(Ambulance ambulance) {
        String sql = "INSERT INTO ambulances(location, quantity) VALUES (?, ?)";
        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ambulance.getLocation());
            stmt.setInt(2, ambulance.getQuantity());

            stmt.executeUpdate();
            return ambulance;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save ambulance: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Ambulance> findById(String location) {
        String sql = "SELECT * FROM ambulances WHERE location = ?";
        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Ambulance ambulance = new Ambulance(
                        rs.getString("location"),
                        rs.getInt("quantity")
                );
                return Optional.of(ambulance);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ambulance: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Ambulance> findAll() {
        List<Ambulance> ambulances = new ArrayList<>();
        String sql = "SELECT * FROM ambulances";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Ambulance ambulance = new Ambulance(
                        rs.getString("location"),
                        rs.getInt("quantity")
                );
                ambulances.add(ambulance);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch ambulances: " + e.getMessage(), e);
        }

        return ambulances;
    }

    @Override
    public void deleteById(String location) {
        String sql = "DELETE FROM ambulances WHERE location = ?";
        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete ambulance: " + e.getMessage(), e);
        }
    }

    @Override
    public Ambulance update(Ambulance ambulance) {
        String sql = "UPDATE ambulances SET quantity = ? WHERE location = ?";
        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ambulance.getQuantity());
            stmt.setString(2, ambulance.getLocation());
            stmt.executeUpdate();

            return ambulance;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update ambulance: " + e.getMessage(), e);
        }
    }
}
