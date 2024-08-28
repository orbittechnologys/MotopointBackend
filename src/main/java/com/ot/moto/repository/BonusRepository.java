package com.ot.moto.repository;

import com.ot.moto.entity.Bonus;
import com.ot.moto.entity.Master;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BonusRepository extends JpaRepository<Bonus, Long> {

    public Page<Bonus> findAll(Pageable pageable);

    /* Method to find the highest bonus where the delivery count is less than or equal to the given count*/
    public Bonus findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(Long deliveryCount);

    /* Method to find the bonus for a specific special date*/
    public Bonus findTopBySpecialDate(LocalDate specialDate);
}