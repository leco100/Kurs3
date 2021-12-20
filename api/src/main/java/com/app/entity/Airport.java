package com.app.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "airports")
@Getter
@Setter
public class Airport {
    @Id
    @JsonProperty("code")
    @NotBlank(message = "код должен быть указан")
    String code;

    @JsonProperty("name")
    @NotBlank(message = "название должно быть указано")
    @Column(nullable = false)
    String name;

    @JsonProperty("city")
    @NotBlank(message = "город должнен быть указан")
    @Column(nullable = false)
    String city;

    @JsonProperty("country")
    @NotNull(message = "страна должна быть указана")
    @ManyToOne
    @PrimaryKeyJoinColumn(referencedColumnName = "country_code")
    Country country;

    @Column
    Boolean visible = true;

    public Airport() {
    }

    public Airport(String code, String name, String city, Country country) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
        this.visible=true;
    }

    @Override
    public String toString() {
        return "Airport{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", country=" + country +
                ", visible=" + visible +
                '}';
    }
}
