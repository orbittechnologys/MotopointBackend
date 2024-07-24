package com.ot.moto.repository;

import com.ot.moto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String Email);

    Optional<User> findByPhone(String phone);
}
