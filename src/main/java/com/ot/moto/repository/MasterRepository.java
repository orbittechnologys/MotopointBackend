package com.ot.moto.repository;

import com.ot.moto.entity.Master;
import com.ot.moto.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master,Long> {

    public Optional<Master> findBySlab(String slab);

    public Page<Master> findAll(Pageable pageable);
}
