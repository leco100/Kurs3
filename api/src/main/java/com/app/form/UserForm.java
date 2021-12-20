
package com.app.form;

import com.app.entity.Users;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserForm extends Users implements Serializable {
    boolean old = false;

    public UserForm() {
    }

    public UserForm(Users users) {
        setLogin(users.getLogin());
        setRole(users.getRole());
        old=true;
    }
}
