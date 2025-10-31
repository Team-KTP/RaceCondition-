package org.kwakmunsu.stock.repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.kwakmunsu.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {

    // 비관적 락을 사용한 재고 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.id = :id")
    Optional<Stock> findByIdWithPessimisticLock(Long id);
}