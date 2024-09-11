package com.ot.moto.dao;

import com.ot.moto.entity.Bonus;
import com.ot.moto.entity.Master;
import com.ot.moto.repository.BonusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Repository
public class BonusDao {

    @Autowired
    private BonusRepository bonusRepository;

    public Bonus saveBonus(Bonus bonus) {
        return bonusRepository.save(bonus);
    }

    public Bonus findById(Long id) {
        Optional<Bonus> bonus = bonusRepository.findById(id);
        return bonus.orElse(null);
    }

    public Page<Bonus> findAll(int offset, int pageSize, String field) {
        return bonusRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).ascending()));
    }

    public void delete(Bonus bonus) {
        bonusRepository.delete(bonus);
    }

    public Bonus findTopBySpecialDate(LocalDate specialDate) {
        return bonusRepository.findTopBySpecialDate(specialDate);
    }

    public Bonus findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(Long deliveryCount) {
        return bonusRepository.findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(deliveryCount);
    }

  /*  public Bonus findTopByDistanceTravelledLessThanEqualOrderByDistanceTravelledDesc(Long distanceTravelled) {
        return bonusRepository.findTopByDistanceTravelledLessThanEqualOrderByDistanceTravelledDesc(distanceTravelled);
    }*/
}
