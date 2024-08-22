package com.ot.moto.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class UpdateMasterReq {

        private String slab;
        private Long startKm;
        private Long endKm;
        private Double jahezPaid;
        private Double motoPaid;

    }

