package com.rebalance.repository;

import com.rebalance.entity.Rebalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RebalanceRepository extends JpaRepository<Rebalance, Long> {
	/*
		Обеспечивает доступ к данным бюджета в БД
	*/
}
