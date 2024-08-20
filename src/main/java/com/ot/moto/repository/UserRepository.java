package com.ot.moto.repository;

import com.ot.moto.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    public Optional<User> findByEmail(String Email);

    public Optional<User> findByPhone(String phone);
}
