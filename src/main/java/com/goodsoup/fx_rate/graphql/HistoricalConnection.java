package com.goodsoup.fx_rate.graphql;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import java.util.List;

public record HistoricalConnection(int totalCount, List<HistoricalEdge> edges, PageInfo pageInfo) {
    public record HistoricalEdge(HistoricalEntity node, String cursor) {}
    public record PageInfo(String endCursor, boolean hasNextPage) {}
}

