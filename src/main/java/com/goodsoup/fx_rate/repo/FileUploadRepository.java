package com.goodsoup.fx_rate.repo;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileUploadRepository extends JpaRepository<FileUploadEntity, Long> {
    Optional<FileUploadEntity> findByFileUploadUuid(UUID fileUploadUuid);

    int countByPair_Id(Long pairId);

    List<FileUploadEntity> findByPair_IdOrderByCreatedAtDescIdDesc(Long pairId, org.springframework.data.domain.Pageable pageable);

    @Query("""
            select u
            from FileUploadEntity u
            where u.pair.id = :pairId
              and (u.createdAt < :afterCreatedAt
                   or (u.createdAt = :afterCreatedAt and u.id < :afterId))
            order by u.createdAt desc, u.id desc
            """)
    List<FileUploadEntity> findPageByPairAfter(
            @Param("pairId") Long pairId,
            @Param("afterCreatedAt") OffsetDateTime afterCreatedAt,
            @Param("afterId") Long afterId,
            org.springframework.data.domain.Pageable pageable
    );
}

