package com.app.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.method.P;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
@Getter
@Setter
public class Flight implements Serializable {
    @Id
    @NotBlank(message = "Код рейса должен быть указан")
    String code;

    @JsonProperty("dateDep")
    @Column(name = "date_dep")
    @NotNull(message = "Дата вылета должна быть указана")
    LocalDate dateDep;

    @JsonProperty("timeDep")
    @NotNull(message = "Время вылета должно быть указано")
    @Column(name = "time_dep")
    LocalTime timeDep;

    // аэропорт отправления
    @JsonProperty("areaSrc")
    @NotNull(message = "аэропорт отправления должен быть указан")
    @ManyToOne
    @PrimaryKeyJoinColumn(name = "source_airport")
    Airport areaSrc;


    // аэропорт назначения
    @JsonProperty("areaDestination")
    @NotNull(message = "аэропорт назначения должен быть указан")
    @ManyToOne
    @PrimaryKeyJoinColumn(name = "destination_airport")
    Airport areaDestination;

    @ManyToOne
    @NotNull(message = "Компания должна быть указана")
    @PrimaryKeyJoinColumn(name = "company")
    Company company;

    @ManyToOne
    @NotNull(message = "Тип самолета  должен быть указан")
    @PrimaryKeyJoinColumn(name = "airplane")
    Airplane airplane;

    // время полетам в минутах
    @JsonProperty("flightTime")
    @NotNull(message = "время полета должно быть указано")
    @Min(10)
    @Column(nullable = false)
    int flightTime;

    @JsonProperty("dateArrival")
    @Column(name = "date_arrival")
    LocalDate dateArrival;

    @JsonProperty("timeArrival")
    @Column(name = "time_arrival")
    LocalTime timeArrival;

    @JsonProperty("addTime")
    @Column(nullable = true)
    int addTime = 0;

    @JsonProperty("status")
    @NotNull(message = "Статус полета должен быть указан")
    @Enumerated(EnumType.STRING)
    FlyStatus status = FlyStatus.SCHEDULED;

   

    @Column
    Boolean active = true;


    public Flight() {
    }

    @PrePersist
    public void ins(){
       LocalDateTime date= LocalDateTime.of(dateDep,timeDep);
       date = date.plusMinutes(flightTime);
       dateArrival = date.toLocalDate();
       timeArrival = date.toLocalTime();
    }
    @PreUpdate
    public void upd(){
        LocalDateTime date= LocalDateTime.of(dateDep,timeDep);
        date = date.plusMinutes(flightTime);
        if (addTime>0)
            date = date.plusMinutes(addTime);
        dateArrival = date.toLocalDate();
        timeArrival = date.toLocalTime();
    }
}
