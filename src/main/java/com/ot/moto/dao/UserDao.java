package com.ot.moto.dao;

import com.ot.moto.entity.User;
import com.ot.moto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserDao {

    @Autowired
    private UserRepository userRepository;


    public User save(User user) {
        return userRepository.save(user);
    }

    public User findByOtp(String otp) {
        return userRepository.findByOtp(otp);
    }

    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public boolean checkUserExists(String email, Long phone) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return true;
        }

        Optional<User> userByPhone = userRepository.findByPhone(phone);
        return userByPhone.isPresent();
    }

    public Optional<User> getUser(String email, Long phone) {
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            return userByEmail;
        }

        Optional<User> userByPhone = userRepository.findByPhone(phone);
        return userByPhone;
    }

    public Optional<User> getUserByPhone(Long phone) {
        Optional<User> userByPhone = userRepository.findByPhone(phone);
        return userByPhone;
    }

    public Optional<User> getUserByEmail(String email) {
        Optional<User> userByPhone = userRepository.findByEmail(email);
        return userByPhone;
    }

    public User updateUserStatus(Long userId, boolean status) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setStatus(status);
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }
}