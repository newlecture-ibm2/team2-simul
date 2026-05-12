package com.simul.tryon.application.port.out;

public interface SafeSearchPort {
    SafeSearchResult analyze(byte[] imageBytes);

    record SafeSearchResult(
            Likelihood adult,
            Likelihood violence,
            Likelihood racy
    ) {
        public boolean isInappropriate() {
            return adult.isHighOrAbove() || violence.isHighOrAbove() || racy.isHighOrAbove();
        }
    }

    enum Likelihood {
        UNKNOWN,
        VERY_UNLIKELY,
        UNLIKELY,
        POSSIBLE,
        LIKELY,
        VERY_LIKELY;

        public boolean isHighOrAbove() {
            return this == LIKELY || this == VERY_LIKELY;
        }
    }
}

