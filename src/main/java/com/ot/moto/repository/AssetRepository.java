package com.ot.moto.repository;

import com.ot.moto.entity.Asset;
import com.ot.moto.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    public List<Asset> findByItemContaining(String item);

    public List<Asset> findByDriver(Driver driver);
}