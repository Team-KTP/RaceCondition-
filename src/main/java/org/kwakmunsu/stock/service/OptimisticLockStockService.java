package org.kwakmunsu.stock.service;

import lombok.RequiredArgsConstructor;
import org.kwakmunsu.stock.entity.Stock;
import org.kwakmunsu.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OptimisticLockStockService {

    private final StockRepository stockRepository;


    @Transactional
    public Long decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(id).orElseThrow();

        stock.decrease(quantity);

        return stock.getQuantity();
    }

}