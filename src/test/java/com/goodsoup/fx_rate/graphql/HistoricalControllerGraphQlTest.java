package com.goodsoup.fx_rate.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.entity.PairEntity;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@GraphQlTest(HistoricalController.class)
@Import(GraphqlConfig.class)
class HistoricalControllerGraphQlTest {

    @Autowired
    GraphQlTester graphQlTester;

    @MockitoBean
    HistoricalRepository historicalRepository;

    @Test
    void historicalByPair_returnsConnection() {
        PairEntity pair = new PairEntity();
        pair.setId(10L);
        pair.setBase("EUR");
        pair.setQuote("USD");

        HistoricalEntity h = new HistoricalEntity();
        h.setId(5L);
        h.setPair(pair);
        h.setDate(LocalDate.parse("2026-01-01"));
        h.setHigh(new BigDecimal("1.10000"));
        h.setLow(new BigDecimal("1.09000"));
        h.setClose(new BigDecimal("1.09500"));

        when(historicalRepository.findByPair_IdOrderByDateDescIdDesc(org.mockito.ArgumentMatchers.eq(10L), any()))
                .thenReturn(List.of(h));
        when(historicalRepository.countByPair_Id(10L)).thenReturn(1);

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int) {
                          historicalByPair(pairId: $pairId, first: $first) {
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
                              endCursor
                              hasNextPage
                            }
                          }
                        }
                        """)
                .variable("pairId", "10")
                .variable("first", 1)
                .execute()
                .path("historicalByPair.totalCount").entity(Integer.class).isEqualTo(1)
                .path("historicalByPair.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false)
                .path("historicalByPair.edges[0].node.id").entity(String.class).isEqualTo("5")
                .path("historicalByPair.edges[0].node.date").entity(String.class).isEqualTo("2026-01-01")
                .path("historicalByPair.edges[0].node.high")
                .entity(BigDecimal.class)
                .satisfies(v -> Assertions.assertThat(v).isEqualByComparingTo("1.10000"));
    }

    @Test
    void historical_byId_returnsEntity() {
        long id = 1L;
        PairEntity pair = new PairEntity();
        pair.setId(10L);
        pair.setBase("EUR");
        pair.setQuote("USD");

        HistoricalEntity h = new HistoricalEntity();
        h.setId(id);
        h.setPair(pair);
        h.setDate(LocalDate.parse("2026-01-01"));
        h.setHigh(new BigDecimal("1.10000"));
        h.setLow(new BigDecimal("1.09000"));
        h.setClose(new BigDecimal("1.09500"));

        when(historicalRepository.findById(id)).thenReturn(Optional.of(h));

        graphQlTester
                .document("""
                        query($id: ID!) {
                          historical(id: $id) {
                            id
                            date
                            high
                            low
                            close
                          }
                        }
                        """)
                .variable("id", id)
                .execute()
                .path("historical.id").entity(String.class).isEqualTo("1")
                .path("historical.date").entity(String.class).isEqualTo("2026-01-01")
                .path("historical.high")
                .entity(BigDecimal.class)
                .satisfies(v -> Assertions.assertThat(v).isEqualByComparingTo("1.10000"));
    }

    @Test
    void historical_byId_returnsNullWhenNotFound() {
        when(historicalRepository.findById(1L)).thenReturn(Optional.empty());

        graphQlTester
                .document("""
                        query($id: ID!) {
                          historical(id: $id) {
                            id
                          }
                        }
                        """)
                .variable("id", 1L)
                .execute()
                .path("historical").valueIsNull();
    }

    @Test
    void historicalByPair_withAfter_returnsConnectionWithNextPage() {
        long pairId = 10L;

        LocalDate afterDate = LocalDate.parse("2026-01-15");
        long afterId = 100L;
        String afterCursor = CursorUtil.encodeHistoricalCursor(afterDate, afterId);

        PairEntity pair = new PairEntity();
        pair.setId(pairId);
        pair.setBase("EUR");
        pair.setQuote("USD");

        HistoricalEntity first = new HistoricalEntity();
        first.setId(101L);
        first.setPair(pair);
        first.setDate(LocalDate.parse("2026-01-14"));
        first.setHigh(new BigDecimal("1.10000"));
        first.setLow(new BigDecimal("1.09000"));
        first.setClose(new BigDecimal("1.09500"));

        HistoricalEntity second = new HistoricalEntity();
        second.setId(102L);
        second.setPair(pair);
        second.setDate(LocalDate.parse("2026-01-13"));
        second.setHigh(new BigDecimal("1.20000"));
        second.setLow(new BigDecimal("1.19000"));
        second.setClose(new BigDecimal("1.19500"));

        when(historicalRepository.findPageByPairAfter(
                        org.mockito.ArgumentMatchers.eq(pairId),
                        org.mockito.ArgumentMatchers.eq(afterDate),
                        org.mockito.ArgumentMatchers.eq(afterId),
                        any()))
                .thenReturn(List.of(first, second));
        when(historicalRepository.countByPair_Id(pairId)).thenReturn(2);

        String expectedCursor = CursorUtil.encodeHistoricalCursor(first.getDate(), first.getId());

        graphQlTester
                .document("""
                        query($pairId: ID!, $first: Int, $after: String) {
                          historicalByPair(pairId: $pairId, first: $first, after: $after) {
                            totalCount
                            edges {
                              cursor
                              node { id date }
                            }
                            pageInfo { endCursor hasNextPage }
                          }
                        }
                        """)
                .variable("pairId", pairId)
                .variable("first", 1)
                .variable("after", afterCursor)
                .execute()
                .path("historicalByPair.totalCount").entity(Integer.class).isEqualTo(2)
                .path("historicalByPair.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(true)
                .path("historicalByPair.pageInfo.endCursor").entity(String.class).isEqualTo(expectedCursor)
                .path("historicalByPair.edges[0].cursor").entity(String.class).isEqualTo(expectedCursor)
                .path("historicalByPair.edges[0].node.id").entity(String.class).isEqualTo("101");
    }
}

