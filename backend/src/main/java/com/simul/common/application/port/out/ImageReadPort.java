package com.simul.common.application.port.out;

public interface ImageReadPort {
    ImageReadResult read(String imageUrl);

    record ImageReadResult(byte[] bytes, String mimeType) {
    }
}

