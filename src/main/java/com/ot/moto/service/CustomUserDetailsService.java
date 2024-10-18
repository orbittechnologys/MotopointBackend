package com.ot.moto.service;

import com.ot.moto.entity.User;
import com.ot.moto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> optional;

        // Try to find the user by email first
        optional = userRepository.findByEmail(identifier);
        if (optional.isPresent()) {
            return new CustomUserDetails(optional.get());
        }

        // If not found by email, try to find by phone number
        optional = userRepository.findByPhone(identifier);
        if (optional.isPresent()) {
            return new CustomUserDetails(optional.get());
        }

        // If neither found, throw exception
        throw new UsernameNotFoundException("Invalid email or phone number: " + identifier);
    }
}