package com.example.demo.service.impl;

import com.example.demo.domain.entity.Dataset;
import com.example.demo.domain.entity.DatasetVersion;
import com.example.demo.domain.entity.User;
import com.example.demo.exception.ApiException;
import com.example.demo.repository.DatasetRepository;
import com.example.demo.repository.DatasetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetRepository datasetRepo;
    private final DatasetVersionRepository versionRepo;

    private final Path storageRoot = Paths.get("data/files");

    @Transactional
    public DatasetVersion createAndUpload(String name, User owner, MultipartFile file)
            throws IOException {

        Dataset dataset = datasetRepo
                .findByNameAndOwner(name, owner)
                .orElseGet(() -> {
                    Dataset d = new Dataset();
                    d.setName(name);
                    d.setOwner(owner);
                    return datasetRepo.save(d);
                });

        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();

        Path datasetDir = storageRoot.resolve(String.valueOf(dataset.getId()));
        Files.createDirectories(datasetDir);

        Path destination = datasetDir.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        DatasetVersion version = new DatasetVersion();
        version.setDataset(dataset);
        version.setStoragePath(destination.toString());
        version.setStatus(DatasetVersion.Status.PENDING);

        version = versionRepo.save(version);

        processAsync(version.getId());

        return version;
    }

    @Async
    public void processAsync(Long versionId) {

        DatasetVersion version = versionRepo.findById(versionId)
                .orElseThrow(() -> new ApiException("Dataset version not found"));

        try {
            version.setStatus(DatasetVersion.Status.PROCESSING);
            versionRepo.save(version);

            Path path = Paths.get(version.getStoragePath());

            List<String> lines = Files.readAllLines(path);
            int rowCount = lines.size();

            version.setRowCount(rowCount);
            version.setStatus(DatasetVersion.Status.DONE);

            versionRepo.save(version);

        } catch (Exception e) {
            version.setStatus(DatasetVersion.Status.FAILED);
            versionRepo.save(version);
        }
    }
}

