package com.example.demo.repository;

import com.example.demo.domain.entity.Dataset;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    List<Dataset> findByOwner(User owner);
}