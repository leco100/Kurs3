package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.WrongUserException;
import com.app.model.Users;
import com.app.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;


@Configuration
@PropertySource("file:./server.conf")
public class UserServiceImpl implements UserService {
    @Value("${baseUrl}")
    private String baseUrl;

    private static String authUrl = "/api/user/auth";
    private static String createUrl = "/api/user/create";
    private static String saveUrl = "/api/user";

    @Autowired
    ApiConfig apiConfig;


    @Autowired
    private RestTemplate restTemplateAuth;

    private RestTemplate restTemplate;


    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    private HttpHeaders headers;


    @Autowired
    MultiValueMap<String, String> headersMap;


    /**
     * авторизация пользователя
     * @param login логин
     * @param password параоль
     * @return пользователя
     * @throws WrongUserException если не верный логин пароль
     */
    @Override
    public Users authUser(String login, String password) throws WrongUserException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(authUrl));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("login", login);
        map.add("password", password);

        HttpEntity<Users> request = new HttpEntity(map, headers);
        restTemplateAuth.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        try {
            // оправим запрос и прочитаем результат
            ResponseEntity<Users> response = restTemplateAuth.postForEntity(builder.toUriString(), request, Users.class);
            Users user = response.getBody();
            user.setPassword(password);
            apiConfig.setUser(user);
            restTemplate = apiConfig.getTemplate();
            return user;
        }catch (ResourceAccessException e){
            throw  new ApiException("Сервер не доступен!");

            // этот блок вызовется если сервер вернет что-то типа не верный пароль
        }catch (Exception e){
            e.printStackTrace();
            //String error = e.getResponseBodyAsString();
            String error = e.getMessage();
            error =  error.replace("400 Bad Request: ","");
            throw new WrongUserException(error);
        }
    }

    @Override
    public Users saveUser(Users user) throws RuntimeException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Users> request = new HttpEntity(user, headers);

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        try {
            ResponseEntity<Users> response = restTemplate.postForEntity(builder.toUriString(), request, Users.class);
            return response.getBody();
        }catch (Exception e){
            String error = e.getMessage();
            error =  error.replace("400 Bad Request: ","");
            throw new ApiException(error);
        }

    }

    @Override
    public Users createUser(Users user) throws RuntimeException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(createUrl));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("login", user.getLogin());
        map.add("password", user.getPassword());
        map.add("firstName", user.getFirstName());
        map.add("lastName", user.getLastName());
        map.add("role", user.getRole().getName());

        HttpEntity<Users> request = new HttpEntity(map, headers);
        restTemplateAuth.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        try {
            // оправим запрос и прочитаем результат
            ResponseEntity<Users> response = restTemplateAuth.postForEntity(builder.toUriString(), request, Users.class);
            return user;
        }catch (ResourceAccessException e){
            throw  new ApiException("Сервер не доступен!");

            // этот блок вызовется если сервер вернет что-то типа не верный пароль
        }catch (Exception e){
            e.printStackTrace();
            //String error = e.getResponseBodyAsString();
            String error = e.getMessage();
            error =  error.replace("400 Bad Request: ","");
            throw new ApiException(error);
        }
    }
}
