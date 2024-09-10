package com.ot.moto.repository;

import com.ot.moto.entity.Visa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisaRepository extends JpaRepository<Visa, Long> {

    public Optional<Visa> findByVisaNameIgnoreCase(String visaName);
}