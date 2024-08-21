package com.ot.moto.repository;

import com.ot.moto.entity.OrgReports;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrgReportsRepository extends JpaRepository<OrgReports,Long> {

    public OrgReports findByDidAndDispatchTime(Long did, LocalDateTime dispatchTime);

    public List<OrgReports> findByDriverId(String driverId);
}
