package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String driverName;
    private LocalDate date;
    private long noOfS1;
    private long noOfS2;
    private long noOfS3;
    private long noOfS4;
    private long noOfS5;
    private long totalOrders;
    private double codAmount;
    private double credit;
    private double debit;

    @ManyToOne
    @JoinColumn
    @JsonManagedReference("driver")
    private Driver driver;
}
