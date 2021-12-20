package com.app.entity;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;


@Entity
@Getter
@Setter
public class Airplane implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty("id")
    long id;
    // модель
    @JsonProperty("model")
    @NotBlank(message = "Модель не может быть пустой")
    @Column(nullable = false,unique = true)
    String model;

    @JsonProperty("name")
    @NotBlank(message = "Название не может быть пустой")
    @Column(nullable = false)
    String name;

    // кол-во мест
    @JsonProperty("economySeats")
    @NotNull(message = "кол-во мест в эконом классе должно быть указано")
    @Min(10)
    @Max(300)
    @Column(nullable = false)
    Integer economySeats;

    @JsonProperty("businessSeats")
    @NotNull(message = "кол-во мест в бизне классе должно быть указано")
    @Min(0)
    @Max(100)
    @Column(nullable = false)
    Integer businessSeats;

    // можно ли создавать рейсы на этот самолет
    @JsonProperty("visible")
    @Column
    Boolean visible = true;

    public Airplane() {
    }

    public Airplane(String model, String name, Integer economySeats, Integer businessSeats) {
        this.model = model;
        this.name = name;
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.visible=true;
    }

    public Airplane(long id, String model, String name, Integer economySeats, Integer businessSeats, Boolean visible) {
        this.id = id;
        this.model = model;
        this.name = name;
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.visible = visible;
    }
}

