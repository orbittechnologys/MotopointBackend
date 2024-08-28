package com.ot.moto.dao;

import com.ot.moto.entity.Bonus;
import com.ot.moto.repository.BonusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BonusDao {

    @Autowired
    private BonusRepository bonusRepository;

    public Bonus saveBonus(Bonus bonus) {
        return bonusRepository.save(bonus);
    }

}