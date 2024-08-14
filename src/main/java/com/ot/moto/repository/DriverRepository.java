package com.ot.moto.repository;

import com.ot.moto.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DriverRepository extends JpaRepository<Driver,Long> {

    @Query("SELECT d FROM Driver d WHERE LOWER(d.username) = LOWER(:username)")
    Optional<Driver> findByNameIgnoreCase(@Param("username") String username);

    public Driver findByPhone(String phone);

}
