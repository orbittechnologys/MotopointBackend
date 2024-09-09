package com.ot.moto.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisaType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

}