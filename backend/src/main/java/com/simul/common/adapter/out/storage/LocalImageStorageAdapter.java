package com.simul.common.adapter.out.storage;

import com.simul.common.application.port.out.ImageStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class LocalImageStorageAdapter implements ImageStoragePort {

    private final String basePath;
    private final String urlPrefix;
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "heic");

    public LocalImageStorageAdapter(
            @Value("${simul.storage.local.path:./uploads/images/}") String basePath,
            @Value("${simul.storage.local.url-prefix:/uploads/images/}") String urlPrefix) {
        this.basePath = basePath;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String uploadImage(MultipartFile file, String directoryPrefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 존재하지 않습니다.");
        }

        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다: " + extension);
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        Path uploadDir = Paths.get(basePath, directoryPrefix, datePath);
        
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
            return prefix + directoryPrefix + "/" + datePath + "/" + newFilename;
            
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = imageUrl.replaceFirst("^" + urlPrefix, "");
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }
            
            Path filePath = Paths.get(basePath, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("파일 삭제 실패: " + imageUrl + ", " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
