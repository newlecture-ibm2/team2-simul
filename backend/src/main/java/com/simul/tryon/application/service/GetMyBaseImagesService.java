package com.simul.tryon.application.service;

import com.simul.common.exception.BusinessException;
import com.simul.common.exception.ErrorCode;
import com.simul.tryon.application.dto.BaseImageSummaryResponse;
import com.simul.tryon.application.dto.MyBaseImagesResponse;
import com.simul.tryon.application.port.in.GetMyBaseImagesUseCase;
import com.simul.tryon.application.port.out.BaseImagePersistencePort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMyBaseImagesService implements GetMyBaseImagesUseCase {

    private final BaseImagePersistencePort baseImagePersistencePort;

    @Override
    public MyBaseImagesResponse getMyBaseImages(GetMyBaseImagesQuery query) {
        if (query.getUserId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        List<BaseImageSummaryResponse> baseImages = baseImagePersistencePort.findAllActiveByUserId(query.getUserId())
                .stream()
                .map(it -> new BaseImageSummaryResponse(it.getId(), it.getImageUrl(), it.getCreatedAt()))
                .toList();

        return new MyBaseImagesResponse(baseImages);
    }
}

