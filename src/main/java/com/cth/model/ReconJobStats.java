package com.cth.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recon_job_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconJobStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "file1_name")
    private String file1Name;

    @Column(name = "file2_name")
    private String file2Name;

    @Column(name = "matched_count")
    private int matchedCount;

    @Column(name = "unmatched_count")
    private int unmatchedCount;

    private String status; // SUCCESS, FAILED

    @Column(name = "executed_by")
    private String executedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
