package com.ot.moto.service;

import com.ot.moto.dao.AdminDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateAdminReq;
import com.ot.moto.entity.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private UserDao userDao;

    public ResponseEntity<ResponseStructure<Object>> createAdmin(CreateAdminReq request) {
        try {
            if (userDao.checkUserExists(request.getEmail(), request.getPhone())) {
                logger.warn("Email/ Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email/ Phone already exists");
            }
            Admin admin = buildAdminFromRequest(request);
            adminDao.createAdmin(admin);
            logger.info("Admin created successfully: {}", admin.getId());

            return ResponseStructure.successResponse(admin, "Admin created successfully");

        } catch (Exception e) {
            logger.error("Error creating admin", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Admin buildAdminFromRequest(CreateAdminReq request) {
        Admin admin = new Admin();
        admin.setEmail(request.getEmail());
        admin.setPhone(request.getPhone());
        admin.setPassword(request.getPassword());
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        admin.setProfilePic(request.getProfilePic());
        return admin;
    }
}
