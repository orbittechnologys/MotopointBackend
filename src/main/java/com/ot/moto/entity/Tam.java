package com.ot.moto.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime;
    private String keySessionId;
    private String status;
    private String serviceName;
    private String merchantName;
    private String terminalId;
    private String location;
    private String branchName;
    private String product;
    private Long quantity;
    private String phone;
    private String customerPhone;
    private Long rpnPayment;
    private Long STAN;
    private Double amountToPay;
    private Double payInAmount;
    private String payInAmountData;
    private Double payOutAmount;
    private String payOutAmountData;
    private String paymentMode;
    private Long usedVoucherNumber;
    private Long confirmationId;
    private boolean isVouchered;
    private LocalDateTime holdTrxnDateTime;
    private LocalDateTime confTrxnDateTime;
    private String payInVoucher;
    private String payoutVoucher;
    private Long authCode;
    private Long CPR;
    private Long voucherPhoneNumber;
    private String responseMessage;
    private Long merchantId;
    private Long jahezRiderId;
    private Long cprNumber;
    private String driverCompanyName;
    private String driverCompanyJahezId;
    private Long mobileNumber;
    private String driverName;
}