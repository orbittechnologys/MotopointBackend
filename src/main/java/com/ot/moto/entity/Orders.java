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
    private Long noOfS1;
    private Long noOfS2;
    private Long noOfS3;
    private Long noOfS4;
    private Long noOfS5;
    private Long totalOrders;
    private Double codAmount;
    private Double credit;
    private Double debit;

    @ManyToOne
    @JoinColumn
    @JsonManagedReference("driver")
    private Driver driver;
}
