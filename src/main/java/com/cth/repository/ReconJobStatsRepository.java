package com.cth.repository;

import com.cth.model.ReconJobStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReconJobStatsRepository extends JpaRepository<ReconJobStats, Long> {
    List<ReconJobStats> findTop10ByOrderByCreatedAtDesc();
}
