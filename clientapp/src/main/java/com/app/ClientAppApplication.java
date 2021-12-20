package com.app;

import com.app.config.ApiConfig;
import com.app.model.Role;
import com.app.model.Users;
import com.app.service.UserService;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class ClientAppApplication {

    @Autowired
    UserService service;

    @Autowired
    ApiConfig config;

    public static void main(String[] args) {

        Application.launch(JavaFxApplication.class, args);


    }


}
