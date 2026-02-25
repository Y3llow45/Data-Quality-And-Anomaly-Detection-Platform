package com.example.demo.controller;

import com.example.demo.domain.entity.*;
import com.example.demo.repository.DatasetRepository;
import com.example.demo.repository.DatasetVersionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.DatasetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {
    private final UserRepository userRepository;
    private final DatasetRepository datasetRepo;
    private final DatasetService datasetService;
    private final DatasetVersionRepository versionRepo;

    @PostMapping("/{name}/upload")
    public ResponseEntity<?> upload(@PathVariable String name, @RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User owner = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        DatasetVersion v = datasetService.createAndUpload(name, owner, file);
        return ResponseEntity.accepted().body(Map.of("versionId", v.getId(), "status", v.getStatus()));
    }

    @GetMapping("/versions/{id}/status")
    public ResponseEntity<?> versionStatus(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        DatasetVersion v = versionRepo.findById(id).orElseThrow();
        if (!v.getDataset().getOwner().equals(owner)) {
            throw new AccessDeniedException("Not authorized to access this dataset version");
        }
        List<ColumnStat> stats = datasetService.listColumnStats(id);
        List<Anomaly> anomalies = datasetService.listAnomalies(id);
        return ResponseEntity.ok(Map.of("version", v, "stats", stats, "anomalies", anomalies));
    }

    @GetMapping
    public List<Dataset> listDatasets(@AuthenticationPrincipal UserDetails userDetails) {
        User owner = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        return datasetRepo.findByOwner(owner);
    }

    @GetMapping("/{id}/versions")
    public List<DatasetVersion> listVersions(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Dataset dataset = datasetRepo.findById(id).orElseThrow();
        if (!dataset.getOwner().equals(owner)) {
            throw new AccessDeniedException("Not authorized to access this dataset");
        }
        return versionRepo.findByDatasetId(id);
    }
}