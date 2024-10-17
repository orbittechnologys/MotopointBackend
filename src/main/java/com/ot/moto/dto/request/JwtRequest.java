package com.ot.moto.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequest {

    private String userEmail; // Nullable when using phoneNumber
    private String phoneNumber; // Nullable when using userEmail
    private String password;
}