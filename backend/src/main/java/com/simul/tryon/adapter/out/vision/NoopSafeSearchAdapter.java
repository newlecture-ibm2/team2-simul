package com.simul.tryon.adapter.out.vision;

import com.simul.tryon.application.port.out.SafeSearchPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simul.vision.enabled", havingValue = "false")
public class NoopSafeSearchAdapter implements SafeSearchPort {

    @Override
    public SafeSearchResult analyze(byte[] imageBytes) {
        return new SafeSearchResult(Likelihood.UNKNOWN, Likelihood.UNKNOWN, Likelihood.UNKNOWN);
    }
}

