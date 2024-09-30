package com.ot.moto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    public enum ROLE {
        ROLE_ADMIN, ROLE_STAFF, ROLE_DRIVER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    @Column(unique = true)
    @Size(min = 8, max = 8, message = "Phone number must be exactly 8 digits")
    @Pattern(regexp = "\\d{8}", message = "Phone number must be exactly 8 digits")
    private String phone;

    private String password;

    private String profilePic;

    private LocalDate joiningDate;

    private LocalDate dateOfBirth;

    private String otp;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean status = true;

    private String role;
}