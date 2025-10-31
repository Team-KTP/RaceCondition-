package org.kwakmunsu.stock.repository;

import org.kwakmunsu.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

}