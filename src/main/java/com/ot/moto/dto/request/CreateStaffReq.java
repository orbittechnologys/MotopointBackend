package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateStaffReq {

    private String email;

    private Long phone;

    private String password;

    private String firstName;

    private String lastName;

    private String employeeId;

    private String designation;

    private String profilePic;

    private LocalDate joiningDate;
}
