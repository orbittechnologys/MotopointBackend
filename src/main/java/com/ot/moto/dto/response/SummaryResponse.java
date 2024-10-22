package com.ot.moto.dto.response;

import com.ot.moto.dto.DriverReportDTO;
import com.ot.moto.dto.PaymentReportDTO;
import com.ot.moto.dto.TamReportDTO;
import com.ot.moto.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryResponse {

    private Driver driver;

    private DriverReportDTO driverReport;

    private TamReportDTO tamReport;

    private PaymentReportDTO paymentReport;
}
