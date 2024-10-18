package com.ot.moto.dto.request;

import lombok.Data;

@Data
public class UpdateVisaBikeAmount {
    private Long id;
    private Double visaAmount;
    private Double bikeRentAmount;
}