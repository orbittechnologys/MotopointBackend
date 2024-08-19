package com.ot.moto.repository;

import com.ot.moto.entity.OrgReports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OrgReportsRepository extends JpaRepository<OrgReports,Long> {

    public OrgReports findByDidAndDispatchTime(Long did, LocalDateTime dispatchTime);
}
