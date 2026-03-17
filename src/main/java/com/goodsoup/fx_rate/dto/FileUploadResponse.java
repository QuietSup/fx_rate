package com.goodsoup.fx_rate.dto;

import com.goodsoup.fx_rate.entity.FileUploadStatus;
import java.util.UUID;

public record FileUploadResponse(
        UUID fileUploadUuid,
        FileUploadStatus status,
        Integer rowsLoaded,
        Integer rowsSkipped,
        String errorMessage
) {}

