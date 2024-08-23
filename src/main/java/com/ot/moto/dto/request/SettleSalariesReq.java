package com.ot.moto.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SettleSalariesReq {

    private List<SettleSalary> salaries;
}
