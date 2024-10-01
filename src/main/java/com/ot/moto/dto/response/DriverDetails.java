package com.ot.moto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDetails {

    private long totalDrivers;
    private long attendance;
    private long riders;
    private long drivers;
}
