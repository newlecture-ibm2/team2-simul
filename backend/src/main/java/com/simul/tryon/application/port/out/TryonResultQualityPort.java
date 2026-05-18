package com.simul.tryon.application.port.out;

public interface TryonResultQualityPort {
    TryonResultQualityResult validate(byte[] imageBytes);

    record TryonResultQualityResult(
            boolean valid,
            String reason
    ) {}
}

