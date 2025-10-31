package org.kwakmunsu.stock.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kwakmunsu.stock.entity.Stock;
import org.kwakmunsu.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OptimisticLockStockFacadeTest {

    @Autowired
    OptimisticLockStockFacade facade;

    @Autowired
    StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.save(Stock.create(1L, 100L));
    }

    @AfterEach
    void after() {
        stockRepository.deleteAll();
    }


    @DisplayName("낙관적 락을 이용해 동시에 재고 감소 요청이 100가 들어올 경우 동시성 제어를 한다.")
    @Test
    void optimisticLockStock() throws InterruptedException {

        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    facade.decrease(1L, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isZero();
    }

}