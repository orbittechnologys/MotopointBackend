package com.ot.moto.dao;

import com.ot.moto.entity.Admin;
import com.ot.moto.entity.Staff;
import com.ot.moto.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StaffDao {

    @Autowired
    private StaffRepository staffRepository;

    public Staff createStaff(Staff staff) {
        return staffRepository.save(staff);
    }
}