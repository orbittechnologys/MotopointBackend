package com.ot.moto.dao;

import com.ot.moto.entity.Assets;
import com.ot.moto.repository.AssetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AssetsDao {

    @Autowired
    private AssetsRepository assetsRepository;

    public Assets save(Assets assets) {
        return assetsRepository.save(assets);
    }

    public List<Assets> saveAll(List<Assets> assets) {
        return assetsRepository.saveAll(assets);
    }

    public Assets findById(long id) {
        Optional<Assets> optionalAssets = assetsRepository.findById(id);
        return optionalAssets.orElse(null);
    }

    public Page<Assets> findAll(int offset, int pageSize, String field) {
        return assetsRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).ascending()));
    }
}