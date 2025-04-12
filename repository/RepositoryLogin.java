package repository;

import domain.Login;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryLogin implements IRepository<Login, String> {

    private final JdbcUtils jdbcUtils;
    protected static final Logger logger = LogManager.getLogger();

    public RepositoryLogin(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Login save(Login entity) {
        logger.traceEntry("Saving login: {}", entity);
        String sql = "INSERT INTO login(name, password_hash) VALUES (?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getPasswordHash());
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error saving login: {}", entity, e);
            throw new RuntimeException("Failed to save login: " + e.getMessage(), e);
        }

        logger.trace("Saved login: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }

    @Override
    public Optional<Login> findById(String name) {
        logger.traceEntry("Finding login by name: {}", name);
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
                logger.trace("Found login: {}", login);
                logger.traceExit("Exiting...");
                return Optional.of(login);
            }

        } catch (SQLException e) {
            logger.error("Error finding login with name: {}", name, e);
            throw new RuntimeException("Failed to find login: " + e.getMessage(), e);
        }

        logger.traceExit("No login found with name: {}", name);
        return Optional.empty();
    }

    @Override
    public List<Login> findAll() {
        logger.traceEntry("Fetching all logins");
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
            logger.error("Error fetching logins", e);
            throw new RuntimeException("Failed to fetch logins: " + e.getMessage(), e);
        }

        logger.trace("Fetched {} logins", logins.size());
        logger.traceExit("Exiting...");
        return logins;
    }

    @Override
    public void deleteById(String name) {
        logger.traceEntry("Deleting login with name: {}", name);
        String sql = "DELETE FROM login WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error deleting login with name: {}", name, e);
            throw new RuntimeException("Failed to delete login: " + e.getMessage(), e);
        }

        logger.trace("Deleted login with name: {}", name);
        logger.traceExit("Exiting...");
    }

    @Override
    public Login update(Login entity) {
        logger.traceEntry("Updating login: {}", entity);
        String sql = "UPDATE login SET password_hash = ? WHERE name = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getPasswordHash());
            stmt.setString(2, entity.getName());

            stmt.executeUpdate();

        } catch (SQLException e) {
            logger.error("Error updating login: {}", entity, e);
            throw new RuntimeException("Failed to update login: " + e.getMessage(), e);
        }

        logger.trace("Updated login: {}", entity);
        logger.traceExit("Exiting...");
        return entity;
    }
}
