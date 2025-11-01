package org.kwakmunsu.stock.facade;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.kwakmunsu.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;
    private final StockService stockService;

    public void decrease(Long key, Long quantity) {
        RLock lock = redissonClient.getLock(key.toString());

        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            stockService.decrease(key, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}