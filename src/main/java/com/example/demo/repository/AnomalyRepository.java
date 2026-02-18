package com.example.demo.repository;

import com.example.demo.domain.entity.Anomaly;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnomalyRepository extends JpaRepository<Anomaly, Long> {
    List<Anomaly> findByDatasetVersionId(Long datasetVersionId);
}