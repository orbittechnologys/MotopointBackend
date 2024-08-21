package com.ot.moto.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Master {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String slab;

    Long startKm;

    Long endKm;

    Double jahezPaid;

    Double motoPaid;

}
