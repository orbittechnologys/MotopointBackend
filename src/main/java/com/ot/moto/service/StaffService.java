package com.ot.moto.service;

import com.ot.moto.dao.StaffDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.entity.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    @Autowired
    private StaffDao staffDao;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserDao userDao;

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    public ResponseEntity<ResponseStructure<Object>> createStaff(CreateStaffReq request) {
        try {
            if (userDao.checkUserExists(request.getEmail(), request.getPhone())) {
                logger.warn("Email/ Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email/ Phone already exists");
            }
            Staff staff = buildAdminFromRequest(request);
            staffDao.createStaff(staff);
            logger.info("Staff created successfully: {}", staff.getId());

            return ResponseStructure.successResponse(staff, "Staff created successfully");

        } catch (Exception e) {
            logger.error("Error creating staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Staff buildAdminFromRequest(CreateStaffReq request) {
        Staff staff = new Staff();
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setPassword(encoder.encode(request.getPassword()));
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        staff.setProfilePic(request.getProfilePic());
        staff.setJoiningDate(request.getJoiningDate());
        return staff;
    }


}
