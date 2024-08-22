package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.StaffDao;
import com.ot.moto.dao.UserDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.dto.request.UpdateStaffReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Staff;
import com.ot.moto.entity.User;
import com.ot.moto.repository.StaffRepository;
import com.ot.moto.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffDao staffDao;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StaffRepository staffRepository;

    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    public ResponseEntity<ResponseStructure<Object>> createStaff(CreateStaffReq request) {
        try {
            if (userDao.checkUserExists(request.getEmail(), request.getPhone())) {
                logger.warn("Email/ Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email/ Phone already exists");
            }
            Staff staff = buildStaffFromRequest(request);
            staffDao.createStaff(staff);
            logger.info("Staff created successfully: {}", staff.getId());

            return ResponseStructure.successResponse(staff, "Staff created successfully");

        } catch (Exception e) {
            logger.error("Error creating staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Staff buildStaffFromRequest(CreateStaffReq request) {
        Staff staff = new Staff();
        staff.setEmail(request.getEmail());
        staff.setPhone(request.getPhone());
        staff.setPassword(encoder.encode(request.getPassword()));
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setUsername((request.getFirstName() + " " + request.getLastName()).toUpperCase());
        staff.setProfilePic(request.getProfilePic());
        staff.setJoiningDate(request.getJoiningDate());
        staff.setDesignation(request.getDesignation());
        staff.setEmployeeId(request.getEmployeeId());
        return staff;
    }

    public ResponseEntity<ResponseStructure<Object>> getStaff(Long id) {
        try {
            Staff staff = staffDao.getStaffById(id);
            if (Objects.isNull(staff)) {
                logger.warn("No Staff found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(staff, "Staff found");
        } catch (Exception e) {
            logger.error("Error fetching single staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Staff fetchStaff(Long id) {
        Staff staff = staffDao.getStaffById(id);
        if (Objects.isNull(staff)) {
            logger.warn("No Staff found. Invalid ID:" + id);
            return null;
        }
        return staff;
    }

    private Staff updateStaffFromRequest(UpdateStaffReq request, Staff staff) {
        String firstName = staff.getFirstName();
        String lastName = staff.getLastName();
        if (!StringUtil.isEmpty(request.getEmail())) {
            staff.setEmail(request.getEmail());
        }
        if (!StringUtil.isEmpty(request.getPhone())) {
            staff.setPhone(request.getPhone());
        }
        if (!StringUtil.isEmpty(request.getPassword())) {
            staff.setPassword(encoder.encode(request.getPassword()));
        }
        if (!StringUtil.isEmpty(request.getFirstName())) {
            staff.setFirstName(request.getFirstName());
            firstName = request.getFirstName();
        }
        if (!StringUtil.isEmpty(request.getLastName())) {
            staff.setLastName(request.getLastName());
            lastName = request.getLastName();
        }
        if (!StringUtil.isEmpty(request.getFirstName()) || !StringUtil.isEmpty(request.getLastName())) {
            staff.setUsername((firstName + " " + lastName).toUpperCase());
        }
        if (!StringUtil.isEmpty(request.getProfilePic())) {
            staff.setProfilePic(request.getProfilePic());
        }
        if (!StringUtil.isEmpty(request.getDepartment())) {
            staff.setDesignation(request.getDepartment());
        }
        if (request.getJoiningDate() != null) {
            staff.setJoiningDate(request.getJoiningDate());
        }
        if (request.getEmployeeId() != null) {
            staff.setEmployeeId(request.getEmployeeId());
        }

        return staff;
    }

    public ResponseEntity<ResponseStructure<Object>> updateStaff(UpdateStaffReq request) {
        try {
            Staff staff = fetchStaff(request.getId());
            if (Objects.isNull(staff)) {
                logger.warn("No staff found with id:" + request.getId());
                return ResponseStructure.errorResponse(null, 404, "Staff not found with id:" + request.getId());
            }
            Optional<User> userEmail = userDao.getUserByEmail(request.getEmail());

            if (userEmail.isPresent() && !userEmail.get().getId().equals(request.getId())) {
                logger.warn("Email already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Email already exists");
            }

            Optional<User> userPhone = userDao.getUserByPhone(request.getPhone());

            if (userPhone.isPresent() && !userPhone.get().getId().equals(request.getId())) {
                logger.warn("Phone already exists: {}, {}", request.getEmail(), request.getPhone());
                return ResponseStructure.errorResponse(null, 409, "Phone already exists");
            }
            staff = updateStaffFromRequest(request, staff);
            staffDao.createStaff(staff);

            logger.info("Staff updated successfully: {}", staff.getId());

            return ResponseStructure.successResponse(staff, "Staff updated successfully");

        } catch (Exception e) {
            logger.error("Error updating staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllStaff(int page, int size, String field) {
        try {

            Page<Staff> staffPage = staffDao.findAll(page, size, field);
            if (staffPage.isEmpty()) {
                logger.warn("No Staff found.");
                return ResponseStructure.errorResponse(null, 404, "No Driver found");
            }
            return ResponseStructure.successResponse(staffPage, "Driver found");
        } catch (Exception e) {
            logger.error("Error fetching Staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> deleteStaff(Long staffId) {
        try {
            Staff staff = staffDao.getStaffById(staffId);
            if (Objects.isNull(staff)) {
                logger.warn("Staff not found with ID: {}", staffId);
                return ResponseStructure.errorResponse(null, 404, "Staff not found");
            }

            staffDao.deleteStaff(staff);
            logger.info("Staff deleted successfully: {}", staffId);
            return ResponseStructure.successResponse(null, "Staff deleted successfully");

        } catch (Exception e) {
            logger.error("Error deleting staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<List<Staff>>> findByUsernameContaining(String name) {
        ResponseStructure<List<Staff>> responseStructure = new ResponseStructure<>();

        List<Staff> driverList = staffDao.findByUsernameContaining(name);
        if (driverList.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver Not Found With NAME  ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver Found With NAME ");
            responseStructure.setData(driverList);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }


    public ResponseEntity<InputStreamResource> generateCsvForAllStaff() {
        try {
            List<Staff> allStaff = staffRepository.findAll();
            if (allStaff.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            String[] header = {"Staff ID", "First Name", "Last Name", "Designation", "Employee ID", "Role"};
            csvWriter.writeNext(header);

            for (Staff staff : allStaff) {
                String[] data = {
                        String.valueOf(staff.getId()),
                        staff.getFirstName(),
                        staff.getLastName(),
                        staff.getDesignation(),
                        staff.getEmployeeId(),
                        staff.getRole()
                };
                csvWriter.writeNext(data);
            }

            csvWriter.close();
            String csvContent = writer.toString();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csvContent.getBytes());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=all_staff.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvContent.getBytes().length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}