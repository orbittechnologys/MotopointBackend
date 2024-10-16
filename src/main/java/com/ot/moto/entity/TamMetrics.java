package com.ot.moto.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TamMetrics {

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private long id;

    private Long noOfRowsParsed;

    private Long totalDrivers;

    private Double amountReceived;

    private LocalDateTime dateTime;

    private String fileName;


}
