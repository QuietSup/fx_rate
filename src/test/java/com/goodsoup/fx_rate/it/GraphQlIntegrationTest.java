package com.goodsoup.fx_rate.it;

import com.goodsoup.fx_rate.entity.HistoricalEntity;
import com.goodsoup.fx_rate.entity.PairEntity;
import com.goodsoup.fx_rate.repo.HistoricalRepository;
import com.goodsoup.fx_rate.repo.PairRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
class GraphQlIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    HttpGraphQlTester graphQlTester;

    @Autowired
    PairRepository pairRepository;

    @Autowired
    HistoricalRepository historicalRepository;

    @Test
    void pairs_and_historicalByPair_workAgainstRealDb() {
        PairEntity pair = new PairEntity();
        pair.setBase("EUR");
        pair.setQuote("USD");
        pair = pairRepository.saveAndFlush(pair);

        HistoricalEntity h1 = new HistoricalEntity();
        h1.setPair(pair);
        h1.setDate(LocalDate.parse("2026-01-02"));
        h1.setHigh(new BigDecimal("1.20000"));
        h1.setLow(new BigDecimal("1.10000"));
        h1.setClose(new BigDecimal("1.15000"));
        historicalRepository.saveAndFlush(h1);

        HistoricalEntity h2 = new HistoricalEntity();
        h2.setPair(pair);
        h2.setDate(LocalDate.parse("2026-01-01"));
        h2.setHigh(new BigDecimal("1.10000"));
        h2.setLow(new BigDecimal("1.09000"));
        h2.setClose(new BigDecimal("1.09500"));
        historicalRepository.saveAndFlush(h2);

        graphQlTester
                .document("""
                        query($pairId: ID!) {
                          pairs { id base quote }
                          historicalByPair(pairId: $pairId, first: 10) {
                            totalCount
                            edges { node { id date close } }
                            pageInfo { hasNextPage endCursor }
                          }
                        }
                        """)
                .variable("pairId", pair.getId())
                .execute()
                .path("pairs").entityList(Object.class).hasSize(1)
                .path("pairs[0].base").entity(String.class).isEqualTo("EUR")
                .path("pairs[0].quote").entity(String.class).isEqualTo("USD")
                .path("historicalByPair.totalCount").entity(Integer.class).isEqualTo(2)
                // ordered DESC by date
                .path("historicalByPair.edges[0].node.date").entity(String.class).isEqualTo("2026-01-02")
                .path("historicalByPair.edges[1].node.date").entity(String.class).isEqualTo("2026-01-01")
                .path("historicalByPair.pageInfo.hasNextPage").entity(Boolean.class).isEqualTo(false);
    }
}

