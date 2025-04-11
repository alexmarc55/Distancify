package Repository;

import domain.ApiRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class RepositoryApiRequest implements IRepository<ApiRequest, Integer> {

    private final JdbcUtils jdbcUtils;

    public RepositoryApiRequest(Properties props) {
        this.jdbcUtils = new JdbcUtils(props);
    }

    @Override
    public ApiRequest save(ApiRequest entity) {
        String sql = "INSERT INTO api_requests(endpoint, method, payload, timestamp, status, response) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, entity.getEndpoint());
            stmt.setString(2, entity.getMethod());
            stmt.setString(3, entity.getPayload());
            stmt.setTimestamp(4, entity.getTimestamp());
            stmt.setString(5, entity.getStatus());
            stmt.setString(6, entity.getResponse());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save API request: " + e.getMessage(), e);
        }

        return entity;
    }

    @Override
    public Optional<ApiRequest> findById(Integer id) {
        String sql = "SELECT * FROM api_requests WHERE id = ?";
        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                ApiRequest req = new ApiRequest(
                        rs.getInt("id"),
                        rs.getString("endpoint"),
                        rs.getString("method"),
                        rs.getString("payload"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("status"),
                        rs.getString("response")
                );
                return Optional.of(req);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find API request: " + e.getMessage(), e);
        }

        return Optional.empty();
    }

    @Override
    public List<ApiRequest> findAll() {
        List<ApiRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM api_requests";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ApiRequest req = new ApiRequest(
                        rs.getInt("id"),
                        rs.getString("endpoint"),
                        rs.getString("method"),
                        rs.getString("payload"),
                        rs.getTimestamp("timestamp"),
                        rs.getString("status"),
                        rs.getString("response")
                );
                requests.add(req);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch API requests: " + e.getMessage(), e);
        }

        return requests;
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM api_requests WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete API request: " + e.getMessage(), e);
        }
    }

    @Override
    public ApiRequest update(ApiRequest entity) {
        String sql = "UPDATE api_requests SET endpoint = ?, method = ?, payload = ?, timestamp = ?, status = ?, response = ? WHERE id = ?";

        try (Connection conn = jdbcUtils.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entity.getEndpoint());
            stmt.setString(2, entity.getMethod());
            stmt.setString(3, entity.getPayload());
            stmt.setTimestamp(4, entity.getTimestamp());
            stmt.setString(5, entity.getStatus());
            stmt.setString(6, entity.getResponse());
            stmt.setInt(7, entity.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update API request: " + e.getMessage(), e);
        }

        return entity;
    }
}
