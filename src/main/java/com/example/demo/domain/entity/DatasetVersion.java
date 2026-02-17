package com.example.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "dataset_versions")
@Getter
@Setter
public class DatasetVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(nullable = false)
    private String storagePath; // e.g. files/<datasetId>/<uuid>.csv

    @Column(nullable = false)
    private long rowCount;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, PROCESSING, DONE, FAILED

    private Instant createdAt = Instant.now();

    public enum Status { PENDING, PROCESSING, DONE, FAILED }
}
