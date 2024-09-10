package com.ot.moto.dao;

import com.ot.moto.entity.Asset;
import com.ot.moto.entity.Driver;
import com.ot.moto.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AssetsDao {

    @Autowired
    private AssetRepository assetRepository;

    public List<Asset> saveAll(List<Asset> assets) {
        return assetRepository.saveAll(assets);
    }

    public Asset findById(long id) {
        Optional<Asset> optionalAsset = assetRepository.findById(id);
        return optionalAsset.orElse(null);
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public List<Asset> findByNameContaining(String item) {
        return assetRepository.findByItemContaining(item);
    }

    public void deleteAll(List<Asset> existingAssets) {
        assetRepository.deleteAll();
    }

    public List<Asset> findByDriver(Driver driver) {
        return assetRepository.findByDriver(driver);
    }

    public Asset save(Asset asset) {
        return assetRepository.save(asset);
    }
}
