package com.goodsoup.fx_rate.graphql;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import java.util.List;

public record FileUploadConnection(int totalCount, List<FileUploadEdge> edges, PageInfo pageInfo) {
    public record FileUploadEdge(FileUploadEntity node, String cursor) {}
}

