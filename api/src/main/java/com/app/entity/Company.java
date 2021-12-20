package com.app.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * класс Company
 * компания, от имени которой осуществялется перелет
 */
@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    // название
    @JsonProperty("name")
    @NotBlank(message = "Название компании должно быть указано")
    @Column(nullable = false)
    String name;

    // почтовый ящик
    @JsonProperty("email")
    @NotBlank(message = "email должен быть указан")
    @Column(nullable = false)
    String email;

    // телефон
    @JsonProperty("phone")
    @NotBlank(message = "телефон должен быть указан")
    @Column(nullable = false)
    String phone;
    // адрес офиса
    @JsonProperty("address")
    @NotBlank(message = "адрес должен быть указан")
    @Column(nullable = false)
    String address;
    // страна
    @JsonProperty("country")
    @NotNull(message = "страна должна быть указана")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "county_code",nullable = false)
    Country country;

    boolean visible;

    public Company() {
    }

    public Company(String name, String email, String phone, String address, Country country) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.country = country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}

