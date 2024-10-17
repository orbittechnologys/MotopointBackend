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
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AuthenticateService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticateService.class);

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService detailsService;

    public ResponseEntity<ResponseStructure<JwtResponse>> authenticate(@RequestBody JwtRequest jwtRequest)
            throws Exception {
        CustomUserDetails details;
        if (jwtRequest.getUserEmail() != null && !jwtRequest.getUserEmail().isEmpty()) {
            // Authenticate by email
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(jwtRequest.getUserEmail(), jwtRequest.getPassword()));
            } catch (BadCredentialsException e) {
                logger.error(e.getMessage());
                throw new Exception("Invalid Email or Password", e);
            }
            details = (CustomUserDetails) detailsService.loadUserByUsername(jwtRequest.getUserEmail());
        } else if (jwtRequest.getPhoneNumber() != null && !jwtRequest.getPhoneNumber().isEmpty()) {
            // Authenticate by phone number
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(jwtRequest.getPhoneNumber(), jwtRequest.getPassword()));
            } catch (BadCredentialsException e) {
                logger.error(e.getMessage());
                throw new Exception("Invalid Phone Number or Password", e);
            }
            details = (CustomUserDetails) detailsService.loadUserByPhoneNumber(jwtRequest.getPhoneNumber());
        } else {
            throw new Exception("Email or Phone number is required for authentication");
        }

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
        responseStructure.setData(new JwtResponse(jwt, details.getUser()));
        return ResponseEntity.ok(responseStructure);
    }
}
