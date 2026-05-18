package com.simul.tryon.adapter.out.ai;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.port.out.TryonAiGenerationPort;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GeminiTryonAiAdapter implements TryonAiGenerationPort {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    @Value("${simul.gemini.api-key:}")
    private String apiKey;

    @Value("${simul.gemini.model:gemini-2.5-flash-image}")
    private String model;

    @Override
    @SuppressWarnings("unchecked")
    public TryonAiGenerationResult generate(TryonAiGenerationCommand command) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "GEMINI API key is not configured");
        }

        String url = "/v1beta/models/" + model + ":generateContent";

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", command.prompt()));
        parts.add(Map.of("inline_data", Map.of(
                "mime_type", command.userImageMimeType(),
                "data", Base64.getEncoder().encodeToString(command.userImageBytes())
        )));
        for (TryonAiGenerationPort.ImagePart imagePart : command.clothingImages()) {
            parts.add(Map.of("inline_data", Map.of(
                    "mime_type", imagePart.mimeType(),
                    "data", Base64.getEncoder().encodeToString(imagePart.bytes())
            )));
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", parts
                        )
                )
        );

        Map<String, Object> response = restClient.post()
                .uri(url)
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.getFirst().get("content");
            List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");

            // Gemini API 응답은 환경/버전에 따라 `inlineData`(camelCase) 또는 `inline_data`(snake_case)로 내려올 수 있습니다.
            // 여러 개의 inline image가 섞여 내려오는 경우가 있어 마지막 inline image를 결과로 사용합니다.
            TryonAiGenerationResult lastInlineImage = null;
            for (Map<String, Object> part : responseParts) {
                Map<String, Object> inlineData = null;
                if (part.containsKey("inlineData")) {
                    inlineData = (Map<String, Object>) part.get("inlineData");
                } else if (part.containsKey("inline_data")) {
                    inlineData = (Map<String, Object>) part.get("inline_data");
                }
                if (inlineData == null) {
                    continue;
                }

                String mimeType = (String) (inlineData.get("mimeType") != null ? inlineData.get("mimeType") : inlineData.get("mime_type"));
                String data = (String) inlineData.get("data");
                if (mimeType == null || data == null || data.isBlank()) {
                    continue;
                }

                lastInlineImage = new TryonAiGenerationResult(Base64.getDecoder().decode(data), mimeType);
            }

            if (lastInlineImage != null) {
                return lastInlineImage;
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "Gemini response parse failed: " + e.getMessage());
        }

        throw new BusinessException(ErrorCode.AI_GENERATION_FAILED, "Gemini returned no inline image data");
    }
}
