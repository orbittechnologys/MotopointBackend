package com.ot.moto.service;

import com.ot.moto.entity.User;
import com.ot.moto.repository.UserRepository;
import lombok.SneakyThrows;
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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<User> optional = userRepository.findByEmail(email);

        if (optional.isEmpty()) {
            throw new UsernameNotFoundException("Invalid user email: " + email);
        }

        User user = optional.get();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        return customUserDetails;
    }

}