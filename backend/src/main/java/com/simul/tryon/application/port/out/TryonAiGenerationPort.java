package com.simul.tryon.application.port.out;

public interface TryonAiGenerationPort {
    TryonAiGenerationResult generate(TryonAiGenerationCommand command);

    record TryonAiGenerationCommand(
            byte[] userImageBytes,
            String userImageMimeType,
            java.util.List<ImagePart> clothingImages,
            String prompt
    ) {
    }

    record ImagePart(
            byte[] bytes,
            String mimeType
    ) {
    }

    record TryonAiGenerationResult(
            byte[] resultImageBytes,
            String resultImageMimeType
    ) {
    }
}
