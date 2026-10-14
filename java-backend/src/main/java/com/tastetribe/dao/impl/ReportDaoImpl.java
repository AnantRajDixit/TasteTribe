package com.tastetribe.dao.impl;

import com.tastetribe.dao.ReportDao;
import com.tastetribe.model.Report;
import com.tastetribe.model.ReportTargetType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link ReportDao} (moderation queue). */
@Repository
public class ReportDaoImpl implements ReportDao {

    private static final RowMapper<Report> MAPPER = ReportDaoImpl::mapRow;

    private final NamedParameterJdbcTemplate jdbc;

    public ReportDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Report mapRow(ResultSet rs, int rowNum) throws SQLException {
        Report report = new Report();
        report.setId(rs.getString("id"));
        report.setTargetType(ReportTargetType.from(rs.getString("target_type")));
        report.setTargetId(rs.getString("target_id"));
        report.setReason(rs.getString("reason"));
        report.setReporterId(rs.getString("reporter_id"));
        report.setStatus(rs.getString("status"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return report;
    }

    @Override
    public void save(Report report) {
        jdbc.update(
                "INSERT INTO reports (id, target_type, target_id, reason, reporter_id, status, created_at) "
                        + "VALUES (:id, :targetType, :targetId, :reason, :reporterId, :status, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", report.getId())
                        .addValue("targetType", report.getTargetType().name())
                        .addValue("targetId", report.getTargetId())
                        .addValue("reason", report.getReason())
                        .addValue("reporterId", report.getReporterId())
                        .addValue("status", report.getStatus() == null ? "PENDING" : report.getStatus())
                        .addValue("createdAt", report.getCreatedAt()));
    }

    @Override
    public Optional<Report> findById(String id) {
        List<Report> rows = jdbc.query("SELECT * FROM reports WHERE id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public List<Report> findPending() {
        return jdbc.query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT 200",
                new MapSqlParameterSource(), MAPPER);
    }

    @Override
    public void updateStatus(String id, String status) {
        jdbc.update("UPDATE reports SET status = :status WHERE id = :id",
                new MapSqlParameterSource().addValue("id", id).addValue("status", status));
    }
}
