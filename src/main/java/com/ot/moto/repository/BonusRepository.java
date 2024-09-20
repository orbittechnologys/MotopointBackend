package com.ot.moto.repository;

import com.ot.moto.entity.Bonus;
import com.ot.moto.entity.Master;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BonusRepository extends JpaRepository<Bonus, Long> {

    public Page<Bonus> findAll(Pageable pageable);

    public Bonus findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(Long deliveryCount);

    public Bonus findTopBySpecialDate(LocalDate specialDate);

    public Page<Bonus> findAllByDeliveryCountIsNotNull(Pageable pageable);

    public Page<Bonus> findAllBySpecialDateIsNotNull(Pageable pageable);

}

/*Method to find the highest bonus where the distance travelled is less than or equal to the given distance
    public Bonus findTopByDistanceTravelledLessThanEqualOrderByDistanceTravelledDesc(Long distanceTravelled);*/