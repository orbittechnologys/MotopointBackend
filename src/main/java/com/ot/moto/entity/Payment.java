package com.ot.moto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Payment {

    public enum PAYMENT_TYPE{
        BENEFIT, TAM, CASH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    private String description;

    private String type;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;
}