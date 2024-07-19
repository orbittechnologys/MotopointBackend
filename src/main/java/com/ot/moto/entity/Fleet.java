package com.ot.moto.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Data
public class Fleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleName;

    private String vehicleType;

    private LocalDate insuranceExpiryDate;

    private String insuranceDocument;

    @OneToOne
    @JoinColumn
    @JsonManagedReference("fleet")
    private Driver driver;

}
