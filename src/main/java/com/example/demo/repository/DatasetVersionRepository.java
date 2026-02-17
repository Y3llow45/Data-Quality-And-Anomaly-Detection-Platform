package com.example.demo.repository;
import com.example.demo.domain.entity.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, Long> {
}
