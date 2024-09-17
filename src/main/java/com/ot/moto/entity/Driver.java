package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver extends User {

    {
        super.setRole("ROLE_DRIVER");
    }

    private Double amountPending;

    private Double amountReceived;

    private Long totalOrders;

    private Long currentOrders;

    private Double salaryAmount;

    private Double payToJahez = 0.0;

    private Double codAmount = 0.0;

    private Double bonus = 0.0;

    private Double paidByTam = 0.0;

    private Double profit = 0.0;

    private String jahezId;

    private LocalDate visaExpiryDate;

    private String address;

    private String referenceLocation;

    private String nationality;

    private String passportNumber;

    private LocalDate passportExpiryDate;

    @Size(min = 9, max = 9, message = "CPR Number must be exactly 9 digits")
    @Pattern(regexp = "\\d{9}", message = "CPR Number must be exactly 9 digits")
    private String cprNumber;

    private String vehicleType;//Frontend Drop down as Owned, Rented, Source Rented

    private String vehicleNumber; // If Vehicle Type Owned

    private String dlType;//Frontend Drop down as PRIVATE , MOTERCYCLE

    private LocalDate dlExpiryDate;

    private String bankAccountName;

    private String bankName;

    private String bankAccountNumber;

    private String bankIbanNumber;

    private String bankBranch;

    private String bankBranchCode;

    private String bankSwiftCode;

    private String bankAccountCurrency;//Frontend Drop down as BHD

    private String bankMobilePayNumber;//Frontend Name it as Benefit Pay Number

    private String bankAccountType;//Frontend Drop Down

    private Double visaAmount;
    private LocalDate visaAmountStartDate;
    private LocalDate visaAmountEndDate;
    private Double visaAmountEmi;

    private Double bikeRentAmount;
    private LocalDate bikeRentAmountStartDate;
    private LocalDate bikeRentAmountEndDate;
    private Double bikeRentAmountEmi;

    private Double otherDeductionAmount;
    private LocalDate otherDeductionAmountStartDate;
    private LocalDate otherDeductionAmountEndDate;
    private Double otherDeductionsAmountEmi;

    private String remarks;
    private String deductionDescription;


    //Document Uploads
    private String dlFrontPhotoUrl;
    private String dlBackPhotoUrl;

    private String rcFrontPhotoUrl;
    private String rcBackPhotoUrl;

    private String passbookImageUrl;

    private String passportFrontUrl;
    private String passportBackUrl;

    private String cprFrontImageUrl;
    private String cprBackImageUrl;

    private String cprReaderImageUrl;

    private String visaCopyImageUrl;

    private String consentDoc;



    @OneToOne(mappedBy = "driver")
    @JsonBackReference("fleet")
    private Fleet fleet;

    @OneToMany(mappedBy = "driver")
    @JsonBackReference("driver")
    private List<Orders> orders;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("assets")
    private List<Asset> assets;

    @ManyToOne
    @JoinColumn
    @JsonManagedReference("visa")
    private Visa visa;

}