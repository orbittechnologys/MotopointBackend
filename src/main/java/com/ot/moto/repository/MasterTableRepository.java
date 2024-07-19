package com.ot.moto.repository;

import com.ot.moto.entity.MasterTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterTableRepository extends JpaRepository<MasterTable,Long> {
}
