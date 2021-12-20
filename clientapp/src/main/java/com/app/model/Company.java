package com.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * класс Company
 * компания, от имени которой осуществялется перелет
 */

@Getter
@Setter
@Builder
public class Company implements Serializable {

    @JsonProperty("id")
    Long id;

    // название
    @JsonProperty("name")
    String name;

    // почтовый ящик
    @JsonProperty("email")
    String email;

    // телефон
    @JsonProperty("phone")
    String phone;
    // адрес офиса
    @JsonProperty("address")
    String address;
    // страна
    @JsonProperty("country")
    Country country;

    boolean visible = true;

    public Company() {
    }

    public Company(Long id, String name, String email, String phone, String address, Country country, boolean visible) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.country = country;
        this.visible = visible;
    }

    @Override
    public String toString() {
        return  name;
    }
}

