package org.kwakmunsu.stock.facade;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kwakmunsu.stock.service.OptimisticLockStockService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService stockService;

    /**
     * * 낙관적 락 실패 시 재시도 로직
     **/
    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.decrease(id, quantity);
                break;
            } catch (ObjectOptimisticLockingFailureException e) {
                Thread.sleep(50);
            }
        }
    }

}