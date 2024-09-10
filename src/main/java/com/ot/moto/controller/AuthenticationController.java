package com.ot.moto.controller;

import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.JwtRequest;
import com.ot.moto.dto.response.JwtResponse;
import com.ot.moto.service.AuthenticateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/authenticate")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    @Autowired
    private AuthenticateService authenticateService;

    @Operation(summary = "Authenticate User By Email and password", description = "Input is Email and Password Request, returns JWT Token Of that Object")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "SUCCESS")})
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseStructure<JwtResponse>> authenticate(@RequestBody JwtRequest jwtRequest)
            throws Exception {
        return authenticateService.authenticate(jwtRequest);
    }
}