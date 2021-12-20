package com.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;


@Getter
@Setter
@Builder
public class Airplane implements Serializable {
    @JsonProperty("id")
    long id;
    // модель
    @JsonProperty("model")
    String model;

    @JsonProperty("name")
    String name;

    // кол-во мест
    @JsonProperty("economySeats")
    Integer economySeats;

    @JsonProperty("businessSeats")
    Integer businessSeats;

    // можно ли создавать рейсы на этот самоле
    @JsonProperty("visible")
    boolean visible = true;

    public Airplane() {
    }

    public Airplane(long id, String model, String name, Integer economySeats, Integer businessSeats, boolean visible) {
        this.id = id;
        this.model = model;
        this.name = name;
        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.visible = visible;
    }

    @Override
    public String toString() {
        return name;
    }
}

