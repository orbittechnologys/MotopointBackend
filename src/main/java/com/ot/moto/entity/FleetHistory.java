package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FleetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profit;

    private LocalDateTime fleetAssignDateTime;

    private LocalDateTime fleetUnAssignDateTime;

    @ManyToOne
    @JoinColumn(name = "fleet_id")
    @JsonBackReference("fleetHistory")
    private Fleet fleet;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    @JsonBackReference("driverHistory")
    private Driver driver;
}
