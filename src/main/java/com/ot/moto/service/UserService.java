package com.ot.moto.service;

import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.User;
import com.ot.moto.util.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder encoder;


    private static final Logger logger = LoggerFactory.getLogger(TamService.class);

    public ResponseEntity<ResponseStructure<String>> forgotPassword(String email) {
        ResponseStructure<String> responseStructure = new ResponseStructure<>();
        Optional<User> userOptional = userDao.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Email does not exist: " + email);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            User user = userOptional.get();
            String otp = String.valueOf((int) (Math.random() * (9999 - 1000) + 1000));
            user.setOtp(otp);
            userDao.save(user);
            emailSender.sendEmail(user.getEmail(), "This is Your OTP \n" +
                            " Don't Share OTP with Anyone\n " +
                            "Enter this OTP To Update Password \n" + " -> OTP " + otp,
                    "Your OTP To Update Password");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("OTP sent to email ID: " + email);
            responseStructure.setData("OTP sent to the email of user");
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<User>> validateOtp(String otp) {
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByOtp(otp);
        if (user != null) {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setData(user);
            responseStructure.setMessage("Success");
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } else {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setData(null);
            responseStructure.setMessage("OTP invalid");
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<ResponseStructure<User>> updateNewPassword(String password, String otp) {
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        User user = userDao.findByOtp(otp);
        if (Objects.isNull(user)) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("OTP invalid, password not updated");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            user.setPassword(encoder.encode(password));
            user.setOtp(null);
            userDao.save(user);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Password updated for " + user.getEmail());
            responseStructure.setData(user);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }
}
