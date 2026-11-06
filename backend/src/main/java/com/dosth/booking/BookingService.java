package com.dosth.booking;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {
    private final JdbcTemplate jdbcTemplate;

    public BookingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Atomic conditional booking claim to avoid race conditions[cite: 2, 6, 8]
    @Transactional
    public boolean acceptBooking(UUID bookingId, UUID helperId) {
        String sql = "UPDATE bookings SET helper_id = ?, status = 'ASSIGNED' WHERE id = ? AND status = 'SEARCHING'";
        int rows = jdbcTemplate.update(sql, helperId, bookingId);
        return rows > 0;
    }

    // Validate OTP and initiate server timer[cite: 2, 6]
    @Transactional
    public boolean startJob(UUID bookingId, String enteredOtp) {
        String sql = "UPDATE bookings SET status = 'STARTED', started_at = ? WHERE id = ? AND status = 'EN_ROUTE' AND otp_start = ?";
        return jdbcTemplate.update(sql, Instant.now(), bookingId, enteredOtp) > 0;
    }

    // Final payable amount calculation enforcing 30-minute rounding and minimum-hour blocks
    @Transactional
    public void completeJob(UUID bookingId) {
        Instant endTime = Instant.now();
        var row = jdbcTemplate.queryForMap("SELECT started_at, rate_snapshot FROM bookings WHERE id = ?", bookingId);
        Instant startTime = ((java.sql.Timestamp) row.get("started_at")).toInstant();
        double rate = ((Number) row.get("rate_snapshot")).doubleValue();

        long minutes = Duration.between(startTime, endTime).toMinutes();
        // Min 1 hour (60 min), then round up to next 30 min intervals
        long billableMinutes = Math.max(60, (long) (Math.ceil(minutes / 30.0) * 30));
        double finalAmount = (billableMinutes / 60.0) * rate;

        jdbcTemplate.update("UPDATE bookings SET status = 'COMPLETED', ended_at = ?, total_amount = ? WHERE id = ?",
                endTime, finalAmount, bookingId);
    }
}
