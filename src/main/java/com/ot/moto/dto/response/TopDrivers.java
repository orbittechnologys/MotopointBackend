package com.ot.moto.dto.response;

import com.ot.moto.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDrivers {

    private Driver topDriverByTotalOrders;

    private Driver topDriverByCurrentOrders;
}
