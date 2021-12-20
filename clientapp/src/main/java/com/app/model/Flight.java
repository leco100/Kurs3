package com.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
public class Flight implements Serializable {
    @JsonProperty("code")
    String code;

    @JsonProperty("dateDep")
    LocalDate dateDep;

    @JsonProperty("timeDep")
    LocalTime timeDep;


    @JsonProperty("dateArrival")
    LocalDate dateArrival;

    @JsonProperty("timeArrival")
    LocalTime timeArrival;

    // аэропорт отправления
    @JsonProperty("areaSrc")
    Airport areaSrc;


    // аэропорт назначения
    @JsonProperty("areaDestination")
    Airport areaDestination;

    // время полетам в минутах
    @JsonProperty("flightTime")
    int flightTime;

    @JsonProperty("addTime")
    int addTime = 0;

    @JsonProperty("status")
    FlyStatus status;



    @JsonProperty("company")
    Company company;

    @JsonProperty("airplane")
    Airplane airplane;

    boolean visible = true;


    public Flight() {
    }

    public Flight(String code, LocalDate dateDep, LocalTime timeDep, LocalDate dateArrival, LocalTime timeArrival, Airport areaSrc, Airport areaDestination, int flightTime, int addTime, FlyStatus status,  Company company, Airplane airplane, boolean visible) {
        this.code = code;
        this.dateDep = dateDep;
        this.timeDep = timeDep;
        this.dateArrival = dateArrival;
        this.timeArrival = timeArrival;
        this.areaSrc = areaSrc;
        this.areaDestination = areaDestination;
        this.flightTime = flightTime;
        this.addTime = addTime;
        this.status = status;
        this.company = company;
        this.airplane = airplane;
        this.visible = visible;
    }
}
