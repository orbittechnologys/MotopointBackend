package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class UpdateAdminReq {

    private Long id;

    private String email;

    private String phone;

    private String firstName;

    private String lastName;

    private String profilePic;
}
