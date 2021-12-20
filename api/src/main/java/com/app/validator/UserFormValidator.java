package com.app.validator;


import com.app.entity.Users;
import com.app.form.UserForm;
import com.app.repos.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserFormValidator implements Validator {
    private  final String PASSWORD_PATTERN = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,40})";
    Pattern pattern;
    Matcher matcher;

    @Autowired
    UsersRepository userRepository;
    @Override
    public boolean supports(Class<?> clazz) {
        return  clazz == UserForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserForm form = (UserForm) target;
        if (!form.isOld()){
            Optional<Users> user = userRepository.findById(form.getLogin());
            if (user.isPresent()){
                errors.rejectValue("login", "Duplicate.userForm.login");
            }
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "login", "NotEmpty.userFrom.login");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.passForm.password");
            if (!form.getPassword().isEmpty() && !validate(PASSWORD_PATTERN, form.getPassword())) {
                errors.rejectValue("password", "Match.userForm.password");
            }

        }
        if (form.getRole()==null){
            errors.rejectValue("roles", "NotEmpty.userForm.role");
        }
    }
    private boolean validate(String pat, String str) {
        pattern = Pattern.compile(pat);
        matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
