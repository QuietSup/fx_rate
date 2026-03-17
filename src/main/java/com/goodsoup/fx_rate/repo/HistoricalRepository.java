package com.goodsoup.fx_rate.repo;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricalRepository extends JpaRepository<HistoricalEntity, Long> {}

