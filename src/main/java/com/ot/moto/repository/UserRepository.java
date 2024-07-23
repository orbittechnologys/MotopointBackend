package com.ot.moto.repository;

import com.ot.moto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

    User findByEmail(String Email);

    User findByPhone(String phone);
}
