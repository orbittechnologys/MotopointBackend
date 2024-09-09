package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    public Staff findByPhone(Long phone);

    public List<Staff> findByUsernameContaining(String username);
}
