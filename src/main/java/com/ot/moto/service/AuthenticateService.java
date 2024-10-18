package com.ot.moto.service;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.JwtRequest;
import com.ot.moto.dto.response.JwtResponse;
import com.ot.moto.entity.User;
import com.ot.moto.util.JwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticateService.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService detailsService;

    public ResponseEntity<ResponseStructure<JwtResponse>> authenticate(JwtRequest jwtRequest)
            throws Exception {
        CustomUserDetails details;
        try {
            // Check if email or phone number is provided and authenticate accordingly
            String identifier = getIdentifier(jwtRequest);
            authenticate(identifier, jwtRequest.getPassword());
            details = (CustomUserDetails) detailsService.loadUserByUsername(identifier);

            return buildResponse(details);
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            throw new Exception("Invalid credentials", e);
        }
    }

    private String getIdentifier(JwtRequest jwtRequest) throws Exception {
        if (jwtRequest.getUserEmail() != null && !jwtRequest.getUserEmail().isEmpty()) {
            return jwtRequest.getUserEmail();
        } else if (jwtRequest.getPhoneNumber() != null && !jwtRequest.getPhoneNumber().isEmpty()) {
            return jwtRequest.getPhoneNumber();
        } else {
            throw new Exception("Email or Phone number is required for authentication");
        }
    }

    private void authenticate(String identifier, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, password));
    }

    private ResponseEntity<ResponseStructure<JwtResponse>> buildResponse(CustomUserDetails details) {
        User user = details.getUser();
        if (!user.isStatus()) {
            ResponseStructure<JwtResponse> responseStructure = new ResponseStructure<>();
            responseStructure.setStatus(HttpStatus.FORBIDDEN.value());
            responseStructure.setMessage("Your status is offline. Please contact admin to make you online.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseStructure);
        }

        final String jwt = jwtTokenUtil.generateToken(details);
        ResponseStructure<JwtResponse> responseStructure = new ResponseStructure<>();
        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("SUCCESS");
        responseStructure.setData(new JwtResponse(jwt, user));
        return ResponseEntity.ok(responseStructure);
    }
}