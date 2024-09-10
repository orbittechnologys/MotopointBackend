package com.ot.moto.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Staff extends User {
    {
        super.setRole("ROLE_STAFF");
    }

    private String firstName;

    private String lastName;

    private String designation;

    private String employeeId;

}
