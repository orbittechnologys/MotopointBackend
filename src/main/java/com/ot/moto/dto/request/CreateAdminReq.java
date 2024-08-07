package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAdminReq {

    private String email;

    private String phone;

    private String password;

    private String firstName;

    private String lastName;

    private String profilePic;

    private LocalDate joiningDate;
}