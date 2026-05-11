package com.simul.tryon.application.port.out;

public interface TryonAiGenerationPort {
    TryonAiGenerationResult generate(TryonAiGenerationCommand command);

    record TryonAiGenerationCommand(
            byte[] userImageBytes,
            String userImageMimeType,
            byte[] clothingImageBytes,
            String clothingImageMimeType,
            String prompt
    ) {
    }

    record TryonAiGenerationResult(
            byte[] resultImageBytes,
            String resultImageMimeType
    ) {
    }
}

