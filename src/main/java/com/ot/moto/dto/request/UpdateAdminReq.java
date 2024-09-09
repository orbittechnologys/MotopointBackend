package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class UpdateAdminReq {

    private Long id;

    private String email;

    private Long phone;

    private String firstName;

    private String lastName;

    private String profilePic;
}
