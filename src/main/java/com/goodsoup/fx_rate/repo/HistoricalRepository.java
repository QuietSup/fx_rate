package com.goodsoup.fx_rate.repo;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoricalRepository extends JpaRepository<HistoricalEntity, Long> {
    int countByPair_Id(Long pairId);

    List<HistoricalEntity> findByPair_IdOrderByDateDescIdDesc(Long pairId, org.springframework.data.domain.Pageable pageable);

    @Query("""
            select h
            from HistoricalEntity h
            where h.pair.id = :pairId
              and (h.date < :afterDate or (h.date = :afterDate and h.id < :afterId))
            order by h.date desc, h.id desc
            """)
    List<HistoricalEntity> findPageByPairAfter(
            @Param("pairId") Long pairId,
            @Param("afterDate") LocalDate afterDate,
            @Param("afterId") Long afterId,
            org.springframework.data.domain.Pageable pageable
    );
}

