package com.ot.moto.entity;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
public class Admin  extends User{
    {
        super.setRole("ROLE_ADMIN");
    }

    private String firstName;

    private String lastName;
}
