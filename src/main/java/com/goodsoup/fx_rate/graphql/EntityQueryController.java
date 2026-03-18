package com.goodsoup.fx_rate.graphql;

import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.entity.PairEntity;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import com.goodsoup.fx_rate.repo.PairRepository;
import com.goodsoup.fx_rate.graphql.CursorUtil.DecodedHistoricalCursor;
import com.goodsoup.fx_rate.graphql.CursorUtil.DecodedFileUploadCursor;
import com.goodsoup.fx_rate.graphql.HistoricalConnection.HistoricalEdge;
import com.goodsoup.fx_rate.graphql.FileUploadConnection.FileUploadEdge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class EntityQueryController {

    private final PairRepository pairRepository;
    private final HistoricalRepository historicalRepository;
    private final FileUploadRepository fileUploadRepository;

    public EntityQueryController(
            PairRepository pairRepository,
            HistoricalRepository historicalRepository,
            FileUploadRepository fileUploadRepository
    ) {
        this.pairRepository = pairRepository;
        this.historicalRepository = historicalRepository;
        this.fileUploadRepository = fileUploadRepository;
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<PairEntity> pairs() {
        return pairRepository.findAll();
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public PairEntity pair(@Argument Long id) {
        return pairRepository.findById(id).orElse(null);
    }

    @SchemaMapping(typeName = "Pair", field = "historicals")
    @Transactional(readOnly = true)
    public HistoricalConnection pairHistoricals(PairEntity pair, @Argument Integer first, @Argument String after) {
        return historicalByPair(pair.getId(), first, after);
    }

    @SchemaMapping(typeName = "Pair", field = "fileUploads")
    @Transactional(readOnly = true)
    public FileUploadConnection pairFileUploads(PairEntity pair, @Argument Integer first, @Argument String after) {
        int size = (first == null || first <= 0) ? 100 : Math.min(first, 1000);
        DecodedFileUploadCursor decoded = CursorUtil.decodeFileUploadCursor(after);

        List<FileUploadEntity> itemsPlusOne;
        if (decoded == null) {
            itemsPlusOne = fileUploadRepository.findByPair_IdOrderByCreatedAtDescIdDesc(
                    pair.getId(), PageRequest.of(0, size + 1));
        } else {
            itemsPlusOne = fileUploadRepository.findPageByPairAfter(
                    pair.getId(),
                    decoded.createdAt(),
                    decoded.id(),
                    PageRequest.of(0, size + 1)
            );
        }

        boolean hasNextPage = itemsPlusOne.size() > size;
        List<FileUploadEntity> items = hasNextPage ? itemsPlusOne.subList(0, size) : itemsPlusOne;

        List<FileUploadEdge> edges = new ArrayList<>(items.size());
        String endCursor = null;
        for (FileUploadEntity u : items) {
            String cursor = CursorUtil.encodeFileUploadCursor(u.getCreatedAt(), u.getId());
            edges.add(new FileUploadEdge(u, cursor));
            endCursor = cursor;
        }

        int totalCount = fileUploadRepository.countByPair_Id(pair.getId());
        return new FileUploadConnection(totalCount, edges, new HistoricalConnection.PageInfo(endCursor, hasNextPage));
    }

    @BatchMapping(typeName = "Historical", field = "pair")
    @Transactional(readOnly = true)
    public Map<HistoricalEntity, PairEntity> historicalPair(List<HistoricalEntity> historicals) {
        Set<Long> pairIds = new HashSet<>();
        for (HistoricalEntity h : historicals) {
            PairEntity p = h.getPair();
            if (p != null && p.getId() != null) {
                pairIds.add(p.getId());
            }
        }

        Map<Long, PairEntity> pairsById = new HashMap<>();
        for (PairEntity p : pairRepository.findAllById(pairIds)) {
            pairsById.put(p.getId(), p);
        }

        Map<HistoricalEntity, PairEntity> out = new HashMap<>(historicals.size());
        for (HistoricalEntity h : historicals) {
            PairEntity p = h.getPair();
            out.put(h, (p == null) ? null : pairsById.get(p.getId()));
        }
        return out;
    }

    @BatchMapping(typeName = "FileUpload", field = "pair")
    @Transactional(readOnly = true)
    public Map<FileUploadEntity, PairEntity> fileUploadPair(List<FileUploadEntity> uploads) {
        Set<Long> pairIds = new HashSet<>();
        for (FileUploadEntity u : uploads) {
            PairEntity p = u.getPair();
            if (p != null && p.getId() != null) {
                pairIds.add(p.getId());
            }
        }

        Map<Long, PairEntity> pairsById = new HashMap<>();
        for (PairEntity p : pairRepository.findAllById(pairIds)) {
            pairsById.put(p.getId(), p);
        }

        Map<FileUploadEntity, PairEntity> out = new HashMap<>(uploads.size());
        for (FileUploadEntity u : uploads) {
            PairEntity p = u.getPair();
            out.put(u, (p == null) ? null : pairsById.get(p.getId()));
        }
        return out;
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
        return new HistoricalConnection(totalCount, edges, new HistoricalConnection.PageInfo(endCursor, hasNextPage));
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public List<FileUploadEntity> fileUploads() {
        return fileUploadRepository.findAll();
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public FileUploadEntity fileUpload(@Argument Long id) {
        return fileUploadRepository.findById(id).orElse(null);
    }

    @QueryMapping
    @Transactional(readOnly = true)
    public FileUploadEntity fileUploadByUuid(@Argument String uuid) {
        UUID parsed = UUID.fromString(uuid);
        return fileUploadRepository.findByFileUploadUuid(parsed).orElse(null);
    }
}

