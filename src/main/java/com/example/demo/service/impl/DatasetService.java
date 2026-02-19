package com.example.demo.service.impl;

import com.example.demo.domain.entity.*;
import com.example.demo.domain.enums.Status;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetRepository datasetRepo;
    private final DatasetVersionRepository versionRepo;
    private final ColumnStatRepository columnStatRepo;
    private final AnomalyRepository anomalyRepo;

    private final Path storageRoot = Paths.get("data/files");

    @Transactional
    public DatasetVersion createAndUpload(String datasetName, User owner, MultipartFile file) throws IOException {
        Dataset dataset = datasetRepo.findByNameAndOwner(datasetName, owner)
                .orElseGet(() -> {
                    Dataset d = new Dataset();
                    d.setName(datasetName);
                    d.setOwner(owner);
                    return datasetRepo.save(d);
                });

        Files.createDirectories(storageRoot.resolve(String.valueOf(dataset.getId())));

        String filename = UUID.randomUUID() + "-" + Objects.requireNonNull(file.getOriginalFilename());
        Path destination = storageRoot.resolve(String.valueOf(dataset.getId())).resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        DatasetVersion version = new DatasetVersion();
        version.setDataset(dataset);
        version.setUploadedBy(owner);
        version.setStoragePath(destination.toString());
        version.setStatus(Status.PENDING);

        version = versionRepo.save(version);

        processAsync(version.getId());

        return version;
    }

    @Async
    public void processAsync(Long versionId) {
        DatasetVersion version = versionRepo.findById(versionId).orElseThrow();
        try {
            version.setStatus(Status.PROCESSING);
            versionRepo.save(version);

            Path path = Paths.get(version.getStoragePath());
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String headerLine = reader.readLine();
                if (headerLine == null) throw new RuntimeException("Empty file");

                String[] headers = headerLine.split(",", -1);
                int cols = headers.length;
                List<List<String>> columns = new ArrayList<>(cols);
                for (int i = 0; i < cols; i++) columns.add(new ArrayList<>());

                String line;
                long rowCount = 0;
                while ((line = reader.readLine()) != null) {
                    rowCount++;
                    String[] parts = line.split(",", -1);
                    for (int i = 0; i < cols; i++) {
                        String v = i < parts.length ? parts[i].trim() : "";
                        columns.get(i).add(v.isEmpty() ? null : v);
                    }
                }
                version.setRowCount(rowCount);

                for (int i = 0; i < cols; i++) {
                    String colName = headers[i].trim();
                    List<String> values = columns.get(i);

                    long nulls = values.stream().filter(Objects::isNull).count();
                    double nullRate = rowCount == 0 ? 0.0 : (double) nulls / rowCount;

                    List<Double> numericValues = values.stream()
                            .filter(Objects::nonNull)
                            .map(v -> {
                                try { return Double.parseDouble(v); }
                                catch (Exception ex) { return null; }
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    ColumnStat stat = new ColumnStat();
                    stat.setDatasetVersion(version);
                    stat.setColumnName(colName);
                    stat.setNullRate(nullRate);
                    stat.setDistinctCount((long) values.stream().filter(Objects::nonNull).collect(Collectors.toSet()).size());

                    if (!numericValues.isEmpty() && numericValues.size() >= Math.max(1, rowCount/10)) {
                        stat.setType("NUMERIC");
                        double sum = numericValues.stream().mapToDouble(Double::doubleValue).sum();
                        double avg = sum / numericValues.size();
                        double variance = numericValues.stream().mapToDouble(d -> (d - avg) * (d - avg)).sum() / numericValues.size();
                        double stddev = Math.sqrt(variance);
                        stat.setMinValue(numericValues.stream().mapToDouble(Double::doubleValue).min().orElse(Double.NaN));
                        stat.setMaxValue(numericValues.stream().mapToDouble(Double::doubleValue).max().orElse(Double.NaN));
                        stat.setAvg(avg);
                        stat.setStddev(stddev);
                    } else {
                        stat.setType("STRING");
                    }

                    columnStatRepo.save(stat);

                    if (nullRate >= 0.5) {
                        Anomaly a = new Anomaly();
                        a.setDatasetVersion(version);
                        a.setColumnName(colName);
                        a.setAnomalyType("HIGH_NULL_RATE");
                        a.setSeverity(nullRate >= 0.9 ? "HIGH" : "MEDIUM");
                        a.setDetails("nullRate=" + nullRate);
                        anomalyRepo.save(a);
                    }

                    if ("NUMERIC".equals(stat.getType())) {
                        double avg = stat.getAvg();
                        double stddev = stat.getStddev();
                        if (stddev > 0) {
                            long outliers = numericValues.stream().filter(d -> Math.abs((d - avg) / stddev) > 3.0).count();
                            double frac = (double) outliers / Math.max(1, numericValues.size());
                            if (frac > 0) {
                                Anomaly a = new Anomaly();
                                a.setDatasetVersion(version);
                                a.setColumnName(colName);
                                a.setAnomalyType("OUTLIER");
                                a.setSeverity(frac > 0.05 ? "HIGH" : "LOW");
                                a.setDetails("outliers=" + outliers + ", total_numeric=" + numericValues.size());
                                anomalyRepo.save(a);
                            }
                        }
                    }

                    if ("STRING".equals(stat.getType()) && stat.getDistinctCount() != null) {
                        if (stat.getDistinctCount() <= 3 && rowCount > 10) {
                            Anomaly a = new Anomaly();
                            a.setDatasetVersion(version);
                            a.setColumnName(colName);
                            a.setAnomalyType("LOW_DISTINCT");
                            a.setSeverity("LOW");
                            a.setDetails("distinctCount=" + stat.getDistinctCount());
                            anomalyRepo.save(a);
                        }
                    }
                }

                version.setStatus(Status.DONE);
                versionRepo.save(version);
            }

        } catch (Exception e) {
            version.setStatus(Status.FAILED);
            versionRepo.save(version);
            e.printStackTrace();
        }
    }

    public List<ColumnStat> listColumnStats(Long versionId) { return columnStatRepo.findByDatasetVersionId(versionId); }
    public List<Anomaly> listAnomalies(Long versionId) { return anomalyRepo.findByDatasetVersionId(versionId); }
}