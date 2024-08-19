package com.ot.moto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrgReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long no;
    private Long did;
    private Long refId;
    private String driverName;
    private String driverUsername;
    private String driverId;
    private Double amount;
    private Double price;
    private Double driverDebitAmount;
    private Double driverCreditAmount;
    private Boolean isFreeOrder;
    private LocalDateTime dispatchTime;
    private String subscriber;
    private Boolean driverPaidOrg;
    private Boolean orgSettled;
    private Boolean driverSettled;

}
