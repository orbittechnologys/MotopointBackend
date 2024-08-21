package com.ot.moto.repository;

import com.ot.moto.entity.Master;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MasterRepository extends JpaRepository<Master,Long> {

    public Optional<Master> findBySlab(String slab);
}
