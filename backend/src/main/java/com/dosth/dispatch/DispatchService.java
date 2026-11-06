package com.dosth.dispatch;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class DispatchService {
    private final JdbcTemplate jdbcTemplate;

    public DispatchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Finds verified online helpers within 10 km ordered by distance
    public List<UUID> findNearbyHelpers(double longitude, double latitude, double radiusMeters) {
        String sql = """
            SELECT helper_id FROM helper_locations
            WHERE is_online = true 
            AND ST_DWithin(location, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)
            ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography) ASC
            LIMIT 5
            """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> UUID.fromString(rs.getString("helper_id")),
                longitude, latitude, radiusMeters, longitude, latitude);
    }
}
