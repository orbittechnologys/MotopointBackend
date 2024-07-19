package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class Driver extends  User{
    {
        super.setRole("ROLE_DRIVER");
    }

    private double amountPending;

    private double amountReceived;

    private int totalOrders;

    private int currentOrders;

    private String jahezId;

    private LocalDate visaExpiryDate;

    private boolean hasVisa;

    private boolean hasFlexi;

    private boolean hasOther;

    private double salaryAmount;

    @OneToOne(mappedBy = "driver")
    @JsonBackReference("fleet")
    private Fleet fleet;

    @OneToMany(mappedBy = "driver")
    @JsonBackReference("driver")
    private List<Orders> orders;
}
