package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "column_stats")
@Getter
@Setter
public class ColumnStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dataset_version_id", nullable = false)
    private DatasetVersion datasetVersion;

    @Column(nullable = false)
    private String columnName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private double nullRate;

    private Double minValue;
    private Double maxValue;
    private Double avg;
    private Double stddev;

    private Long distinctCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
