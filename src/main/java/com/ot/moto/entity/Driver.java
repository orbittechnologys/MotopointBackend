package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
public class Driver extends User {
    {
        super.setRole("ROLE_DRIVER");
    }

    private Double amountPending;

    private Double amountReceived;

    private Long totalOrders;

    private Long currentOrders;

    private String jahezId;

    private LocalDate visaExpiryDate;

    private Double salaryAmount;

    private String address;

    private String referenceLocation;

    private String visaType;

    private String visaProcurement;

    private String nationality;

    private String passportNumber;

    private String cprNumber;

    private String vehicleType;

    private String licenceType;

    private String licenceNumber;

    private String licenceExpiryDate;

    private String licensePhotoUrl;

    private String rcPhotoUrl;

    private String bankAccountName;

    private String bankName;

    private String bankAccountNumber;

    private String bankIbanNumber;

    private String bankBranch;

    private String bankBranchCode;

    private String bankSwiftCode;

    private String bankIfsc;

    private String bankAccountCurrency;

    private String bankMobilePayNumber;

    private String passbookImageUrl;

    private Double payToJahez;

    private Double codAmount;

    private Double bonus;

    private Double paidByTam;

    private Double profit;

    @OneToOne(mappedBy = "driver")
    @JsonBackReference("fleet")
    private Fleet fleet;

    @OneToMany(mappedBy = "driver")
    @JsonBackReference("driver")
    private List<Orders> orders;

}