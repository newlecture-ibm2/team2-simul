package com.simul.common.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadBaseDir = "uploads/images/";

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            // 연/월 디렉토리 경로 생성
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/"));
            Path fullDirPath = Paths.get(uploadBaseDir + datePath);

            if (!Files.exists(fullDirPath)) {
                Files.createDirectories(fullDirPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = fullDirPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            return "/" + uploadBaseDir + datePath + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
