package com.ot.moto.repository;

import com.ot.moto.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    public List<Asset> findByItemNameContaining(String item);
}
