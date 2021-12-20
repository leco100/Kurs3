package com.app.config;


import com.app.model.Airport;
import com.app.model.Country;
import com.app.model.Users;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * компонент конфигурации
 */
@Component
@Configuration
@PropertySource("file:./server.conf")
public class ApiConfig {
    // считаем при запуске код аэропорта, который на табло
    @Value("${air.code}")
    private String airCode;


    // пользователь, который прошел авторизацию
    private Users user;


    public void setUser(Users user) {
        this.user = user;
    }

    public Users getUser() {
        return user;
    }





    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();

    }


    // создает rest клиент с basic аутентфикацией
    public RestTemplate getTemplate() {
        return new RestTemplateBuilder()
                    .basicAuthentication(user.getLogin(),user.getPassword())
                    .setConnectTimeout(Duration.ofMillis(3000))
                    .setReadTimeout(Duration.ofMillis(3000))
                    .build();
    }


    @Bean
    public HttpHeaders headers(){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    @Bean
    HttpEntity<String> entityHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity(headers);
        return entity;
    }


    @Bean
    public MultiValueMap<String, String> headersMap(){

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", "application/json");
        return headers;
    }

    //  object mapper  - преобразует json в pojo и обратно
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);  // NON_EMPTY for '' or NULL value
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    //  обновляет в настройка аэропорт по умолчанию
    public void updateProperty(String name,String value){
        String file  = System.getProperty("user.dir").concat("/server.conf");
        try {
            InputStream input = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(input);
            prop.setProperty(name,value);
            input.close();
            OutputStream output = new FileOutputStream(file);
            prop.store(output,"update");
            output.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
