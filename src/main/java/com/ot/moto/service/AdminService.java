package com.ot.moto.service;

import com.ot.moto.dao.AdminDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateAdminReq;
import com.ot.moto.dto.request.UpdateAdminReq;
import com.ot.moto.entity.Admin;
import com.ot.moto.entity.User;
import com.ot.moto.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private PasswordEncoder encoder;

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
        admin.setPassword(encoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        admin.setProfilePic(request.getProfilePic());
        admin.setJoiningDate(request.getJoiningDate());
        return admin;
    }

    private Admin updateAdminFromRequest(UpdateAdminReq request, Admin admin) {
        String firstName = admin.getFirstName();
        String lastName = admin.getLastName();
        if (!StringUtil.isEmpty(request.getEmail())) {
            admin.setEmail(request.getEmail());
        }
        if (!StringUtil.isEmpty(request.getPhone())) {
            admin.setPhone(request.getPhone());
        }
        if (!StringUtil.isEmpty(request.getPassword())) {
            admin.setPassword(encoder.encode(request.getPassword()));
        }
        if (!StringUtil.isEmpty(request.getFirstName())) {
            admin.setFirstName(request.getFirstName());
            firstName = request.getFirstName();
        }
        if (!StringUtil.isEmpty(request.getLastName())) {
            admin.setLastName(request.getLastName());
            lastName = request.getLastName();
        }
        if (!StringUtil.isEmpty(request.getFirstName()) || !StringUtil.isEmpty(request.getLastName())) {
            admin.setUsername((firstName + " " + lastName).toUpperCase());
        }
        if (!StringUtil.isEmpty(request.getProfilePic())) {
            admin.setProfilePic(request.getProfilePic());
        }
        return admin;
    }


    public ResponseEntity<ResponseStructure<Object>> getAdmin(Long id) {
        try {
            Admin admin = adminDao.getAdminById(id);
            if (Objects.isNull(admin)) {
                logger.warn("No Admin found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(admin, "Admin found");
        } catch (Exception e) {
            logger.error("Error fetching single admin", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllAdmin() {
        try {
            List<Admin> admin = adminDao.getAllAdmin();
            if (admin.size() == 0) {
                logger.warn("No Admin found.");
                return ResponseStructure.errorResponse(null, 404, "No Admin found");
            }
            return ResponseStructure.successResponse(admin, "Admin found");
        } catch (Exception e) {
            logger.error("Error fetching admin", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Admin fetchAdmin(Long id) {
        Admin admin = adminDao.getAdminById(id);
        if (Objects.isNull(admin)) {
            logger.warn("No Admin found. Invalid ID:" + id);
            return null;
        }
        return admin;
    }

    public ResponseEntity<ResponseStructure<Object>> updateAdmin(UpdateAdminReq request) {
        try {
            Admin admin = fetchAdmin(request.getId());
            if (Objects.isNull(admin)) {
                logger.warn("No admin found with id:" + request.getId());
                return ResponseStructure.errorResponse(null, 404, "Email/ Phone already exists");
            }
            Optional<User> userEmail =userDao.getUserByEmail(request.getEmail());

            if (userEmail.isPresent() && !userEmail.get().getId().equals(request.getId())) {
                logger.warn("Email already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email already exists");
            }

            Optional<User> userPhone =userDao.getUserByPhone(request.getPhone());

            if (userPhone.isPresent() && !userPhone.get().getId().equals(request.getId())) {
                logger.warn("Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Phone already exists");
            }

            admin = updateAdminFromRequest(request, admin);
            adminDao.createAdmin(admin);
            logger.info("Admin updated successfully: {}", admin.getId());

            return ResponseStructure.successResponse(admin, "Admin updated successfully");

        } catch (Exception e) {
            logger.error("Error updating admin", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}
