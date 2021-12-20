package com.app.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

import javax.persistence.OneToMany;
import java.util.Objects;
import java.util.Set;

/**
 * роль пользователя
 */
@Entity
@Getter
@Setter
public class Role {
    @Id
    String name;

    String info;



    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, String info) {
        this.name = name;
        this.info = info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
