package com.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
public class Country implements Serializable {
    @JsonProperty("code")

    String code;

    @JsonProperty("name")
    String name;

    @JsonProperty("nameEng")
    String nameEng;


    @JsonIgnore
    List<Company> companies;

    boolean visible;


    public Country() {
    }

    public Country(String code, String name, String nameEng, List<Company> companies, boolean visible) {
        this.code = code;
        this.name = name;
        this.nameEng = nameEng;
        this.companies = companies;
        this.visible = visible;
    }

    // будем считать что странны одинаковые если одинаковые коды
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return Objects.equals(code, country.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return name;
    }
}
