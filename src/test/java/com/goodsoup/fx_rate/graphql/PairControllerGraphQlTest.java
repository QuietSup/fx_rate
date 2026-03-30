package com.goodsoup.fx_rate.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.goodsoup.fx_rate.entity.PairEntity;
import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.entity.FileUploadEntity;
import com.goodsoup.fx_rate.entity.FileUploadStatus;
import com.goodsoup.fx_rate.repo.FileUploadRepository;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import com.goodsoup.fx_rate.repo.PairRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(PairController.class)
@Import(GraphqlConfig.class)
class PairControllerGraphQlTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    PairRepository pairRepository;

    @MockitoBean
    HistoricalRepository historicalRepository;

    @MockitoBean
    FileUploadRepository fileUploadRepository;

    @Test
    void pairs_returnsList() {
        PairEntity eurUsd = new PairEntity();
        eurUsd.setId(1L);
        eurUsd.setBase("EUR");
        eurUsd.setQuote("USD");

        when(pairRepository.findAll()).thenReturn(List.of(eurUsd));

        graphQlTester
                .document("""
                        query {
                          pairs {
                            id
                            base
                            quote
                          }
                        }
                        """)
                .execute()
                .path("pairs")
                .entityList(PairEntity.class)
                .hasSize(1)
                .satisfies(items -> {
                    PairEntity p = items.getFirst();
                    org.assertj.core.api.Assertions.assertThat(p.getId()).isEqualTo(1L);
                    org.assertj.core.api.Assertions.assertThat(p.getBase()).isEqualTo("EUR");
                    org.assertj.core.api.Assertions.assertThat(p.getQuote()).isEqualTo("USD");
                });
    }

    @Test
    void pair_returnsEntity() {
        long id = 1L;
        PairEntity eurUsd = new PairEntity();
        eurUsd.setId(id);
        eurUsd.setBase("EUR");
        eurUsd.setQuote("USD");

        when(pairRepository.findById(id)).thenReturn(Optional.of(eurUsd));

        graphQlTester
                .document("""
                        query($id: ID!) {
                          pair(id: $id) {
                            id
                            base
                            quote
                          }
                        }
                        """)
                .variable("id", id)
                .execute()
                .path("pair.id").entity(String.class).isEqualTo("1")
                .path("pair.base").entity(String.class).isEqualTo("EUR")
                .path("pair.quote").entity(String.class).isEqualTo("USD");
    }

    @Test
    void pair_returnsNullWhenNotFound() {
        when(pairRepository.findById(1L)).thenReturn(Optional.empty());

        graphQlTester
                .document("""
                        query($id: ID!) {
                          pair(id: $id) {
                            id
                          }
                        }
                        """)
                .variable("id", 1L)
                .execute()
                .path("pair").valueIsNull();
    }

    @Test
    void pairByBaseQuote_returnsEntity() {
        when(pairRepository.findByBaseAndQuote("EUR", "USD")).thenReturn(Optional.of(newPair(1L, "EUR", "USD")));

        graphQlTester
                .document("""
                        query($base: String!, $quote: String!) {
                          pairByBaseQuote(base: $base, quote: $quote) {
                            id
                            base
                            quote
                          }
                        }
                        """)
                .variable("base", "EUR")
                .variable("quote", "USD")
                .execute()
                .path("pairByBaseQuote.id").entity(String.class).isEqualTo("1")
                .path("pairByBaseQuote.base").entity(String.class).isEqualTo("EUR")
                .path("pairByBaseQuote.quote").entity(String.class).isEqualTo("USD");
    }

    @Test
    void pairByBaseQuote_returnsNullWhenNotFound() {
        when(pairRepository.findByBaseAndQuote("EUR", "USD")).thenReturn(java.util.Optional.empty());

        graphQlTester
                .document("""
                        query($base: String!, $quote: String!) {
                          pairByBaseQuote(base: $base, quote: $quote) {
                            id
                          }
                        }
                        """)
                .variable("base", "EUR")
                .variable("quote", "USD")
                .execute()
                .path("pairByBaseQuote")
                .valueIsNull();
    }

    @Test
    void pairsByBase_returnsList() {
        PairEntity eurUsd = newPair(1L, "EUR", "USD");
        PairEntity eurJpy = newPair(2L, "EUR", "JPY");
        when(pairRepository.findAllByBase("EUR")).thenReturn(List.of(eurUsd, eurJpy));

        graphQlTester
                .document("""
                        query($base: String!) {
                          pairsByBase(base: $base) {
                            id
                            base
                            quote
                          }
                        }
                        """)
                .variable("base", "EUR")
                .execute()
                .path("pairsByBase").entityList(PairEntity.class).hasSize(2)
                .path("pairsByBase[0].base").entity(String.class).isEqualTo("EUR")
                .path("pairsByBase[1].quote").entity(String.class).isEqualTo("JPY");
    }

    @Test
    void pairsByQuote_returnsList() {
        PairEntity eurUsd = newPair(1L, "EUR", "USD");
        PairEntity gbpUsd = newPair(3L, "GBP", "USD");
        when(pairRepository.findAllByQuote("USD")).thenReturn(List.of(eurUsd, gbpUsd));

        graphQlTester
                .document("""
                        query($quote: String!) {
                          pairsByQuote(quote: $quote) {
                            id
                            base
                            quote
                          }
                        }
                        """)
                .variable("quote", "USD")
                .execute()
                .path("pairsByQuote").entityList(PairEntity.class).hasSize(2)
                .path("pairsByQuote[0].base").entity(String.class).isEqualTo("EUR")
                .path("pairsByQuote[1].base").entity(String.class).isEqualTo("GBP");
    }

    @Test
    void pairHistoricals_withoutAfter_returnsConnection() {
        long pairId = 10L;
        PairEntity pair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(pair));

        LocalDate date = LocalDate.parse("2026-01-01");
        long historicalId = 5L;
        HistoricalEntity h = newHistorical(historicalId, pair, date);
        when(historicalRepository.findByPair_IdOrderByDateDescIdDesc(eq(pairId), any()))
                .thenReturn(List.of(h));
        when(historicalRepository.countByPair_Id(pairId)).thenReturn(1);

        String expectedCursor = CursorUtil.encodeHistoricalCursor(date, historicalId);

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int) {
                          pair(id: $pairId) {
                            historicals(first: $first) {
                              totalCount
                              edges {
                                cursor
                                node {
                                  id
                                  date
                                  high
                                  low
                                  close
                                }
                              }
                              pageInfo {
                                hasNextPage
                                endCursor
                              }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .variable("first", 1)
                .execute()
                .path("pair.historicals.totalCount").entity(Integer.class).isEqualTo(1)
                .path("pair.historicals.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false)
                .path("pair.historicals.edges[0].cursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.historicals.pageInfo.endCursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.historicals.edges[0].node.id").entity(String.class).isEqualTo("5")
                .path("pair.historicals.edges[0].node.date").entity(String.class).isEqualTo("2026-01-01");
    }

    @Test
    void pairHistoricals_withAfter_returnsConnectionWithNextPage() {
        long pairId = 10L;
        PairEntity pair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(pair));

        LocalDate afterDate = LocalDate.parse("2026-01-15");
        long afterId = 100L;
        String afterCursor = CursorUtil.encodeHistoricalCursor(afterDate, afterId);

        long firstId = 101L;
        long secondId = 102L;
        HistoricalEntity first = newHistorical(firstId, pair, LocalDate.parse("2026-01-14"));
        HistoricalEntity second = newHistorical(secondId, pair, LocalDate.parse("2026-01-13"));

        when(historicalRepository.findPageByPairAfter(eq(pairId), eq(afterDate), eq(afterId), any()))
                .thenReturn(List.of(first, second));
        when(historicalRepository.countByPair_Id(pairId)).thenReturn(2);

        String expectedCursor = CursorUtil.encodeHistoricalCursor(first.getDate(), first.getId());

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int, $after: String) {
                          pair(id: $pairId) {
                            historicals(first: $first, after: $after) {
                              totalCount
                              edges {
                                cursor
                                node { id date }
                              }
                              pageInfo { hasNextPage endCursor }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .variable("first", 1)
                .variable("after", afterCursor)
                .execute()
                .path("pair.historicals.totalCount").entity(Integer.class).isEqualTo(2)
                .path("pair.historicals.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true)
                .path("pair.historicals.pageInfo.endCursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.historicals.edges[0].node.id").entity(String.class).isEqualTo("101");
    }

    @Test
    void pairHistoricals_resolvesHistoricalPair() {
        long pairId = 10L;
        PairEntity rootPair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(rootPair));

        long historicalPairId = 20L;
        PairEntity historicalPair = newPair(historicalPairId, "GBP", "USD");

        LocalDate date = LocalDate.parse("2026-01-01");
        HistoricalEntity h = newHistorical(5L, historicalPair, date);

        when(historicalRepository.findByPair_IdOrderByDateDescIdDesc(eq(pairId), any()))
                .thenReturn(List.of(h));
        when(historicalRepository.countByPair_Id(pairId)).thenReturn(1);

        when(pairRepository.findAllById(any(Set.class))).thenReturn(List.of(historicalPair));

        graphQlTester
                .document("""
                        query($pairId: ID!) {
                          pair(id: $pairId) {
                            historicals(first: 1) {
                              edges {
                                node {
                                  id
                                  pair {
                                    id
                                    base
                                    quote
                                  }
                                }
                              }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .execute()
                .path("pair.historicals.edges[0].node.id").entity(String.class).isEqualTo("5")
                .path("pair.historicals.edges[0].node.pair.base").entity(String.class).isEqualTo("GBP")
                .path("pair.historicals.edges[0].node.pair.quote").entity(String.class).isEqualTo("USD");
    }

    @Test
    void pairFileUploads_withoutAfter_returnsConnection() {
        long pairId = 10L;
        PairEntity pair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(pair));

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        long uploadId = 7L;
        FileUploadEntity u = newFileUpload(uploadId, pair, createdAt, FileUploadStatus.FINISHED);

        when(fileUploadRepository.findByPair_IdOrderByCreatedAtDescIdDesc(eq(pairId), any()))
                .thenReturn(List.of(u));
        when(fileUploadRepository.countByPair_Id(pairId)).thenReturn(1);

        String expectedCursor = CursorUtil.encodeFileUploadCursor(createdAt, uploadId);

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int) {
                          pair(id: $pairId) {
                            fileUploads(first: $first) {
                              totalCount
                              edges {
                                cursor
                                node {
                                  id
                                  fileUploadUuid
                                  status
                                }
                              }
                              pageInfo { hasNextPage endCursor }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .variable("first", 1)
                .execute()
                .path("pair.fileUploads.totalCount").entity(Integer.class).isEqualTo(1)
                .path("pair.fileUploads.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false)
                .path("pair.fileUploads.edges[0].cursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.fileUploads.pageInfo.endCursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.fileUploads.edges[0].node.id").entity(String.class).isEqualTo("7");
    }

    @Test
    void pairFileUploads_withAfter_returnsConnectionWithNextPage() {
        long pairId = 10L;
        PairEntity pair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(pair));

        OffsetDateTime afterCreatedAt = OffsetDateTime.parse("2026-01-15T00:00:00Z");
        long afterId = 50L;
        String afterCursor = CursorUtil.encodeFileUploadCursor(afterCreatedAt, afterId);

        long firstId = 51L;
        long secondId = 52L;
        FileUploadEntity first = newFileUpload(firstId, pair, OffsetDateTime.parse("2026-01-14T00:00:00Z"), FileUploadStatus.FINISHED);
        FileUploadEntity second = newFileUpload(secondId, pair, OffsetDateTime.parse("2026-01-13T00:00:00Z"), FileUploadStatus.FINISHED);

        when(fileUploadRepository.findPageByPairAfter(eq(pairId), eq(afterCreatedAt), eq(afterId), any()))
                .thenReturn(List.of(first, second));
        when(fileUploadRepository.countByPair_Id(pairId)).thenReturn(2);

        String expectedCursor = CursorUtil.encodeFileUploadCursor(first.getCreatedAt(), first.getId());

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int, $after: String) {
                          pair(id: $pairId) {
                            fileUploads(first: $first, after: $after) {
                              totalCount
                              edges { node { id } cursor }
                              pageInfo { hasNextPage endCursor }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .variable("first", 1)
                .variable("after", afterCursor)
                .execute()
                .path("pair.fileUploads.totalCount").entity(Integer.class).isEqualTo(2)
                .path("pair.fileUploads.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true)
                .path("pair.fileUploads.pageInfo.endCursor").entity(String.class).isEqualTo(expectedCursor)
                .path("pair.fileUploads.edges[0].node.id").entity(String.class).isEqualTo("51");
    }

    @Test
    void pairFileUploads_resolvesFileUploadPair() {
        long pairId = 10L;
        PairEntity pair = newPair(pairId, "EUR", "USD");
        when(pairRepository.findById(pairId)).thenReturn(Optional.of(pair));

        OffsetDateTime createdAt = OffsetDateTime.parse("2026-01-01T00:00:00Z");
        FileUploadEntity u = newFileUpload(7L, pair, createdAt, FileUploadStatus.FINISHED);

        when(fileUploadRepository.findByPair_IdOrderByCreatedAtDescIdDesc(eq(pairId), any()))
                .thenReturn(List.of(u));
        when(fileUploadRepository.countByPair_Id(pairId)).thenReturn(1);

        when(pairRepository.findAllById(any(Set.class))).thenReturn(List.of(pair));

        graphQlTester
                .document("""
                        query($pairId: ID!) {
                          pair(id: $pairId) {
                            fileUploads(first: 1) {
                              edges {
                                node {
                                  id
                                  pair {
                                    id
                                    base
                                    quote
                                  }
                                }
                              }
                            }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .execute()
                .path("pair.fileUploads.edges[0].node.id").entity(String.class).isEqualTo("7")
                .path("pair.fileUploads.edges[0].node.pair.base").entity(String.class).isEqualTo("EUR")
                .path("pair.fileUploads.edges[0].node.pair.quote").entity(String.class).isEqualTo("USD");
    }

    private static PairEntity newPair(long id, String base, String quote) {
        PairEntity p = new PairEntity();
        p.setId(id);
        p.setBase(base);
        p.setQuote(quote);
        return p;
    }

    private static HistoricalEntity newHistorical(long id, PairEntity pair, LocalDate date) {
        HistoricalEntity h = new HistoricalEntity();
        h.setId(id);
        h.setPair(pair);
        h.setDate(date);
        h.setHigh(new BigDecimal("1.10000"));
        h.setLow(new BigDecimal("1.09000"));
        h.setClose(new BigDecimal("1.09500"));
        return h;
    }

    private static FileUploadEntity newFileUpload(long id, PairEntity pair, OffsetDateTime createdAt, FileUploadStatus status) {
        FileUploadEntity u = new FileUploadEntity();
        u.setId(id);
        u.setPair(pair);
        u.setCreatedAt(createdAt);
        u.setFileUploadUuid(java.util.UUID.nameUUIDFromBytes(("upload-" + id).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        u.setStatus(status);
        u.setRowsLoaded(10);
        u.setRowsSkipped(0);
        return u;
    }
}

