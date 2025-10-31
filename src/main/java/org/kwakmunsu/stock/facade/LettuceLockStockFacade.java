package org.kwakmunsu.stock.facade;

import lombok.RequiredArgsConstructor;
import org.kwakmunsu.stock.repository.RedisLockRepository;
import org.kwakmunsu.stock.service.StockService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;


    public void decrease(Long id, Long quantity) throws InterruptedException {
        // lock 획득 할떄까지 시도
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }
        try {
            stockService.decrease(id, quantity);
        } finally {
            // 작업 수행 후 락 해제
            redisLockRepository.unlock(id);
        }
    }

}