package com.goodsoup.fx_rate.graphql;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.graphql.CursorUtil.DecodedHistoricalCursor;
import com.goodsoup.fx_rate.graphql.HistoricalConnection.HistoricalEdge;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class HistoricalController {

    private final HistoricalRepository historicalRepository;

    public HistoricalController(HistoricalRepository historicalRepository) {
        this.historicalRepository = historicalRepository;
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public HistoricalEntity historical(@Argument Long id) {
        return historicalRepository.findById(id).orElse(null);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public HistoricalConnection historicalByPair(
            @Argument Long pairId,
            @Argument Integer first,
            @Argument String after
    ) {
        int size = (first == null || first <= 0) ? 100 : Math.min(first, 1000);
        DecodedHistoricalCursor decoded = CursorUtil.decodeHistoricalCursor(after);

        List<HistoricalEntity> itemsPlusOne;
        if (decoded == null) {
            itemsPlusOne = historicalRepository.findByPair_IdOrderByDateDescIdDesc(pairId, PageRequest.of(0, size + 1));
        } else {
            itemsPlusOne = historicalRepository.findPageByPairAfter(
                    pairId,
                    decoded.date(),
                    decoded.id(),
                    PageRequest.of(0, size + 1)
            );
        }

        boolean hasNextPage = itemsPlusOne.size() > size;
        List<HistoricalEntity> items = hasNextPage ? itemsPlusOne.subList(0, size) : itemsPlusOne;

        List<HistoricalEdge> edges = new ArrayList<>(items.size());
        String endCursor = null;
        for (HistoricalEntity h : items) {
            String cursor = CursorUtil.encodeHistoricalCursor(h.getDate(), h.getId());
            edges.add(new HistoricalEdge(h, cursor));
            endCursor = cursor;
        }

        int totalCount = historicalRepository.countByPair_Id(pairId);
        return new HistoricalConnection(totalCount, edges, new PageInfo(endCursor, hasNextPage));
    }

}

