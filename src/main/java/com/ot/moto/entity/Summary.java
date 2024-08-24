package com.ot.moto.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String driverName;

    private Long deliveries;

    private Long Salary;

    private Double bonus;

    private Double payToJahez;

    private Double paidByTam;

    private Double profit;
}