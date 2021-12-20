package com.app.service;

import com.app.entity.Users;

import com.app.repos.RoleRepository;
import com.app.repos.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class AdminService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UsersRepository userRepository;



    // сброс пароля
    @Transactional
    public String resetPassword(String login, String password) {
        String error = null;
        Users user = null;
        try {
            user = userRepository.findById(login).get();
            if (user != null) {
                user.setPassword(passwordEncoder.encode(password));
                user = userRepository.save(user);
            }
        } // Other error!!
        catch (Exception e) {
            error = e.getLocalizedMessage();
        } // Other error!!
        return error;
    }


}
