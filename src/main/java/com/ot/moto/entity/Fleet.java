package com.ot.moto.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Entity
@Data
public class Fleet {

    public enum OWN_TYPE{
        SELF_OWNED,MOTO_POINT
    }

    public enum VEHICLE_TYPE{
        TWO_WHEELER,FOUR_WHEELER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleName;

    @Column(unique = true,nullable = false)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    private VEHICLE_TYPE vehicleType;

    @Enumerated(EnumType.STRING)
    private OWN_TYPE ownType;

    private LocalDate insuranceExpiryDate;

    private String insuranceDocument;

    private String image;

    private LocalDate fleetAssignDate;

    private String model;

    @OneToOne
    @JoinColumn
    @JsonManagedReference("fleet")
    private Driver driver;
}
