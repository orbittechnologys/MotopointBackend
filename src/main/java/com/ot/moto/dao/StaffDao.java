package com.ot.moto.dao;

import com.ot.moto.entity.Staff;
import com.ot.moto.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StaffDao {

    @Autowired
    private StaffRepository staffRepository;

    public Staff createStaff(Staff staff) {
        return staffRepository.save(staff);
    }

    public Staff getStaffById(Long id) {
        Optional<Staff> adminOptional = staffRepository.findById(id);
        return adminOptional.orElse(null);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Page<Staff> findAll(int offset, int pageSize, String field) {
        return staffRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(field).descending()));
    }
}