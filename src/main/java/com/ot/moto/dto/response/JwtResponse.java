package com.ot.moto.dto.response;

import com.ot.moto.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String jwtToken;

    private User user;

}