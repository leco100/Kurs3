package com.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class Users {
    @JsonProperty
    String login;

    @JsonProperty
    String password;

    @JsonProperty
    String firstName;
    @JsonProperty
    String lastName;

    @JsonProperty
    Role role;

    boolean active;

    public Users() {
    }

    public Users(String login, String password, String firstName, String lastName, Role role, boolean active) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.active = active;
    }

    @Override
    public String toString() {
        return "Users{" +
                "login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role.getName() +
                '}';
    }
}
