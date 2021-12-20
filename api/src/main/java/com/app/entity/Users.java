package com.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;


import javax.persistence.*;

@Entity
@Getter
@Setter
public class Users {
    @Id
    @Column(name = "login")
    String login;

    @JsonIgnore
    @Column(name = "password",length = 128,nullable = false)
    String password;

    @Column(name = "first_name",length = 128,nullable = false)
    String firstName;
    @Column(name = "last_name",length = 128,nullable = false)
    String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    Role role;

    boolean active;

    public Users() {
    }

    public Users(String login, String password, Role role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }
}
