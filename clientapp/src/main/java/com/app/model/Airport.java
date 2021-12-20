package com.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
@Builder
public class Airport {

    @JsonProperty("code")
    String code;

    @JsonProperty("name")
    String name;

    @JsonProperty("city")
    String city;

    @JsonProperty("country")
    Country country;


    boolean visible = true;

    public Airport() {
    }

    public Airport(String code, String name, String city, Country country, boolean visible) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
        this.visible = visible;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(code, airport.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code+" "+name;
    }
}
