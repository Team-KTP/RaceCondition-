package org.kwakmunsu.stock.facade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.stock.entity.Stock;
import org.kwakmunsu.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedissonLockStockFacadeTest {

    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void insert() {
        Stock stock = Stock.create(1L, 100L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    void delete() {
        stockRepository.deleteAll();
    }

    @Test
    void 동시에_100개의요청() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockStockFacade.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        // 100 - (100 * 1) = 0
        assertThat(stock.getQuantity()).isZero();
    }

}