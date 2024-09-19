package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public enum PenaltyStatus {
        SETTLED,
        NOT_SETTLED
    }

    private String description;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PenaltyStatus status;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("fleetPenalty")
    private Fleet fleet;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("driverPenalty")
    private Driver driver;  // Reference to Driver entity
}
