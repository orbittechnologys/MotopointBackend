package com.ot.moto.repository;

import com.ot.moto.entity.Tam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TamRepository extends JpaRepository<Tam, Long> {

    public Tam findBykeySessionId(String session);

}