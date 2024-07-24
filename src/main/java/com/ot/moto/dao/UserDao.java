package com.ot.moto.dao;

import com.ot.moto.entity.User;
import com.ot.moto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class UserDao {

    @Autowired
    private UserRepository userRepository;

    /*public boolean checkUserExists(String email, String phone) {
        Optional<User> user = userRepository.findByEmail(email);
        if (Objects.nonNull(user)) {
            return true;
        }
        user = userRepository.findByPhone(phone);
        if (Objects.nonNull(user)) {
            return true;
        }
        return false;
    }*/

    public boolean checkUserExists(String email, String phone) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return true;
        }

        Optional<User> userByPhone = userRepository.findByPhone(phone);
        return userByPhone.isPresent();
    }

}