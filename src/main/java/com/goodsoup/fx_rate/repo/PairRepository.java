package com.goodsoup.fx_rate.repo;

import com.goodsoup.fx_rate.entity.PairEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PairRepository extends JpaRepository<PairEntity, Long> {
    Optional<PairEntity> findByBaseAndQuote(String base, String quote);

    List<PairEntity> findAllByBase(String base);

    List<PairEntity> findAllByQuote(String quote);
}

