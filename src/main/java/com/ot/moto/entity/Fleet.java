package com.ot.moto.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Data
@Setter
@Getter
public class Fleet {

    public enum VEHICLE_TYPE {
        TWO_WHEELER, FOUR_WHEELER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vehicleName;

    @Column(unique = true,nullable = false)
    private String vehicleNumber;

    private LocalDate insuranceExpiryDate;

    @Enumerated(EnumType.STRING)
    private VEHICLE_TYPE vehicleType;

    private String insuranceDocument;

    @OneToOne
    @JoinColumn
    @JsonManagedReference("fleet")
    private Driver driver;
}
