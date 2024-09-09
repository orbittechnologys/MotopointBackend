package com.ot.moto.entity;

import jakarta.persistence.*;
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

    @Column(unique = true,length = 8) // added on 9th sept
    private long phone;

    private String password;

    private String profilePic;

    private LocalDate joiningDate;

    private String otp;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private boolean status = true;

    private String role;
}
