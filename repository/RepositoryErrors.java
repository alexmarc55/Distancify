package Repository;

import domain.ErrorLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryErrors implements IRepository<ErrorLog, Integer> {

    private final JdbcUtils jdbcUtils;

    public RepositoryErrors(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public ErrorLog save(ErrorLog entity) {
        String sql = "INSERT INTO errors(missed, over_dispatched, created_at) VALUES (?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, entity.getMissed());
            stmt.setInt(2, entity.getOverDispatched());
            stmt.setTimestamp(3, entity.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save error log: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<ErrorLog> findById(Integer id) {
        String sql = "SELECT * FROM errors WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ErrorLog error = new ErrorLog(
                        rs.getInt("id"),
                        rs.getInt("missed"),
                        rs.getInt("over_dispatched"),
                        rs.getTimestamp("created_at")
                );
                return Optional.of(error);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find error log: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<ErrorLog> findAll() {
        List<ErrorLog> errors = new ArrayList<>();
        String sql = "SELECT * FROM errors";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ErrorLog error = new ErrorLog(
                        rs.getInt("id"),
                        rs.getInt("missed"),
                        rs.getInt("over_dispatched"),
                        rs.getTimestamp("created_at")
                );
                errors.add(error);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch errors: " + e.getMessage(), e);
        }

        return errors;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM errors WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete error log: " + e.getMessage(), e);
        }
    }

    @Override
    public ErrorLog update(ErrorLog entity) {
        String sql = "UPDATE errors SET missed = ?, over_dispatched = ?, created_at = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, entity.getMissed());
            stmt.setInt(2, entity.getOverDispatched());
            stmt.setTimestamp(3, entity.getCreatedAt());
            stmt.setInt(4, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update error log: " + e.getMessage(), e);
        }

        return entity;
    }
}
