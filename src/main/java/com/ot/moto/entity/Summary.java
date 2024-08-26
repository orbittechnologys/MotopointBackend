package com.ot.moto.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverName;

    private Long deliveries;

    private Double salary;

    private Double bonus;

    private Double payToJahez;

    private Double paidByTam;

    private Double profit;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;
}
