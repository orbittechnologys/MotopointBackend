package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateStaffReq {

    private Long id;

    private String email;

    private Long phone;

    private String password;

    private String firstName;

    private String lastName;

    private String profilePic;

    private LocalDate joiningDate;

    private String department;

    private String employeeId;
}
