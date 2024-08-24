package com.ot.moto.dao;


import com.ot.moto.entity.Summary;
import com.ot.moto.repository.SummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SummaryDao {

    @Autowired
    private SummaryRepository summaryRepository;

    public Summary findById(Long id) {
        Optional<Summary> summaryOptional = summaryRepository.findById(id);
        return summaryOptional.orElse(null);
    }

    public Page<Summary> findAll(int offset, int pageSize, String field) {
        return summaryRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}
