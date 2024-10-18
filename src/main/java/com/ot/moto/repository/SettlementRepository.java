package com.ot.moto.repository;


import com.ot.moto.entity.Settlement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SettlementRepository  extends JpaRepository<Settlement, Long> {

    public Page<Settlement> findBySettleDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    public Page<Settlement> findAllByDriverId(Long driverId,Pageable pageable);

    public Page<Settlement> findAllByDriverIdAndSettleDateTimeBetween(Long driverId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

}
