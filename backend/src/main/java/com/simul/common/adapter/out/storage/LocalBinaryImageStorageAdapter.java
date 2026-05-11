package com.simul.common.adapter.out.storage;

import com.simul.common.application.port.out.BinaryImageStoragePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalBinaryImageStorageAdapter implements BinaryImageStoragePort {

    private final String basePath;
    private final String urlPrefix;

    public LocalBinaryImageStorageAdapter(
            @Value("${simul.storage.local.path:./uploads/images/}") String basePath,
            @Value("${simul.storage.local.url-prefix:/uploads/images/}") String urlPrefix
    ) {
        this.basePath = basePath;
        this.urlPrefix = urlPrefix;
    }

    @Override
    public String upload(byte[] bytes, String mimeType, String directoryPrefix) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("bytes is empty");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new IllegalArgumentException("mimeType is blank");
        }
        String extension = extensionFromMimeType(mimeType);

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID() + "." + extension;
        Path uploadDir = Paths.get(basePath, directoryPrefix, datePath);

        try {
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(newFilename);
            Files.write(filePath, bytes);

            String prefix = urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
            return prefix + directoryPrefix + "/" + datePath + "/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    private static String extensionFromMimeType(String mimeType) {
        String lower = mimeType.toLowerCase(Locale.ROOT);
        return switch (lower) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/heic" -> "heic";
            default -> "jpg";
        };
    }
}

