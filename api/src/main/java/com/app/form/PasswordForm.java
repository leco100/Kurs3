package com.app.form;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PasswordForm implements Serializable {
    private String login;
    private String password;
    private String confirmPassword;

    public PasswordForm(String login) {
        this.login = login;
    }
}
