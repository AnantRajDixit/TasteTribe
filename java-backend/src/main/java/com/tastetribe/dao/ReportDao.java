package com.tastetribe.dao;

import com.tastetribe.model.Report;
import java.util.List;
import java.util.Optional;

/** Data-access contract for content reports (moderation queue). */
public interface ReportDao {

    void save(Report report);

    Optional<Report> findById(String id);

    List<Report> findPending();

    void updateStatus(String id, String status);
}
