package com.goodsoup.fx_rate.repo;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUploadEntity, Long> {
    Optional<FileUploadEntity> findByFileUploadUuid(UUID fileUploadUuid);
}

