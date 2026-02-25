package com.example.demo.repository;
import com.example.demo.domain.entity.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, Long> {
    List<DatasetVersion> findByDatasetId(Long datasetId);
}
