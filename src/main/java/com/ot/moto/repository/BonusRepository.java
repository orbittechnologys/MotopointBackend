package com.ot.moto.repository;

import com.ot.moto.entity.Bonus;
import com.ot.moto.entity.Master;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BonusRepository extends JpaRepository<Bonus, Long> {
    public Page<Bonus> findAll(Pageable pageable);
}