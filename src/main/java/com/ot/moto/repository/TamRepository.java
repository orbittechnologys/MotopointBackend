package com.ot.moto.repository;

import com.ot.moto.entity.Tam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TamRepository extends JpaRepository<Tam, Long> {

    public Tam findBykeySessionId(String session);

    public Page<Tam> findAll(Pageable pageable);

    public List<Tam> findByJahezRiderId(Long jahezRiderId);

}