package com.simul.common.adapter.out.storage;

import com.simul.common.application.port.out.ImageReadPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ImageReadAdapter implements ImageReadPort {

    private final RestClient restClient = RestClient.create();

    @Value("${simul.storage.local.path:./uploads/images/}")
    private String basePath;

    @Value("${simul.storage.local.url-prefix:/uploads/images/}")
    private String urlPrefix;

    @Override
    public ImageReadResult read(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("imageUrl is blank");
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            byte[] bytes = restClient.get().uri(imageUrl).retrieve().body(byte[].class);
            String mimeType = guessMimeTypeFromUrl(imageUrl);
            return new ImageReadResult(bytes, mimeType);
        }

        String relative = imageUrl;
        if (relative.startsWith(urlPrefix)) {
            relative = relative.substring(urlPrefix.length());
        } else if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }

        Path filePath = Paths.get(basePath).resolve(relative);
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = guessMimeTypeFromUrl(filePath.toString());
            }
            return new ImageReadResult(bytes, mimeType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read image: " + imageUrl, e);
        }
    }

    private static String guessMimeTypeFromUrl(String urlOrPath) {
        String lower = urlOrPath.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".heic")) return "image/heic";
        return "image/jpeg";
    }
}

