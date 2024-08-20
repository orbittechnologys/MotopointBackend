package com.ot.moto.controller;

import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserDao userDao;

    @PostMapping("/{userId}/status")
    public ResponseEntity<ResponseStructure<User>> updateUserStatus(@PathVariable Long userId, @RequestParam boolean status) {
        User user = userDao.updateUserStatus(userId, status);
        String statusMessage = status ? "online" : "offline";
        ResponseStructure<User> responseStructure = new ResponseStructure<>();
        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("User status set to " + statusMessage);
        responseStructure.setData(user);
        return ResponseEntity.ok(responseStructure);
    }

}