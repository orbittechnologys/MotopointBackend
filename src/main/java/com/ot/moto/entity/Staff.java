package com.ot.moto.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Staff extends  User {
    {
        super.setRole("ROLE_STAFF");
    }

    private String firstName;

    private String lastName;

    private String department;

}
