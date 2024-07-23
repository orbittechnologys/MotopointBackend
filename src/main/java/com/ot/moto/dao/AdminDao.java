package com.ot.moto.dao;

import com.ot.moto.entity.Admin;
import com.ot.moto.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class AdminDao {

    @Autowired
    private AdminRepository adminRepository;

    public Admin createAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    public Admin getAdminById(Long id) {
        Optional<Admin> adminOptional = adminRepository.findById(id);
        return adminOptional.orElse(null);
    }

    public List<Admin> getAllAdmin() {
        return adminRepository.findAll();
    }

    public Page<Admin> findAll(int offset, int pageSize, String field) {
        return adminRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}
