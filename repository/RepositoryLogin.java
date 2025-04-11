package Repository;

import domain.Login;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryLogin implements IRepository<Login, String> {

    private final JdbcUtils jdbcUtils;

    public RepositoryLogin(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Login save(Login entity) {
        String sql = "INSERT INTO login(name, password_hash) VALUES (?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getPasswordHash());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save login: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<Login> findById(String name) {
        String sql = "SELECT * FROM login WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Login login = new Login(
                        rs.getString("name"),
                        rs.getString("password_hash")
                );
                return Optional.of(login);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find login: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Login> findAll() {
        List<Login> logins = new ArrayList<>();
        String sql = "SELECT * FROM login";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Login login = new Login(
                        rs.getString("name"),
                        rs.getString("password_hash")
                );
                logins.add(login);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch logins: " + e.getMessage(), e);
        }

        return logins;
    }

    @Override
    public void deleteById(String name) {
        String sql = "DELETE FROM login WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete login: " + e.getMessage(), e);
        }
    }

    @Override
    public Login update(Login entity) {
        String sql = "UPDATE login SET password_hash = ? WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getPasswordHash());
            stmt.setString(2, entity.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update login: " + e.getMessage(), e);
        }

        return entity;
    }
}
