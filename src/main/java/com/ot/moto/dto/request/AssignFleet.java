package com.ot.moto.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignFleet {
    private Long id;
    private Long driverId;
    private LocalDateTime fleetAssignDateTime;
}