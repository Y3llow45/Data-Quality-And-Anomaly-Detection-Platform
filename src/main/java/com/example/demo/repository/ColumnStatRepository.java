package com.example.demo.repository;

import com.example.demo.domain.entity.ColumnStat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ColumnStatRepository extends JpaRepository<ColumnStat, Long> {
    List<ColumnStat> findByDatasetVersionId(Long datasetVersionId);
}
