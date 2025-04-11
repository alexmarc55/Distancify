package Repository;

import domain.Dispatch;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryDispatch implements IRepository<Dispatch, Integer> {

    private final JdbcUtils jdbcUtils;

    public RepositoryDispatch(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public Dispatch save(Dispatch entity) {
        String sql = "INSERT INTO dispatches(source_city, source_county, target_city, target_county, quantity, timestamp) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save dispatch: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<Dispatch> findById(Integer id) {
        String sql = "SELECT * FROM dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dispatch dispatch = new Dispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                return Optional.of(dispatch);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find dispatch: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<Dispatch> findAll() {
        List<Dispatch> dispatches = new ArrayList<>();
        String sql = "SELECT * FROM dispatches";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Dispatch dispatch = new Dispatch(
                        rs.getInt("id"),
                        rs.getString("source_city"),
                        rs.getString("source_county"),
                        rs.getString("target_city"),
                        rs.getString("target_county"),
                        rs.getInt("quantity"),
                        rs.getTimestamp("timestamp")
                );
                dispatches.add(dispatch);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch dispatches: " + e.getMessage(), e);
        }

        return dispatches;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM dispatches WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete dispatch: " + e.getMessage(), e);
        }
    }

    @Override
    public Dispatch update(Dispatch entity) {
        String sql = "UPDATE dispatches SET source_city = ?, source_county = ?, target_city = ?, target_county = ?, quantity = ?, timestamp = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getSourceCity());
            stmt.setString(2, entity.getSourceCounty());
            stmt.setString(3, entity.getTargetCity());
            stmt.setString(4, entity.getTargetCounty());
            stmt.setInt(5, entity.getQuantity());
            stmt.setTimestamp(6, entity.getTimestamp());
            stmt.setInt(7, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update dispatch: " + e.getMessage(), e);
        }

        return entity;
    }
}
