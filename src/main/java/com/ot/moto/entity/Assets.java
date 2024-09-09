package com.ot.moto.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String item;

    private long quantity;

    private LocalDate localDate;

    @ManyToOne
    @JoinColumn
    @JsonBackReference("assets")
    private Driver driver;
}