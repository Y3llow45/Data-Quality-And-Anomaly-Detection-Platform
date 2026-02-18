package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "anomalies")
@Getter
@Setter
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dataset_version_id", nullable = false)
    private DatasetVersion datasetVersion;

    @Column(nullable = false)
    private String columnName;

    @Column(nullable = false)
    private String anomalyType;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "text")
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
