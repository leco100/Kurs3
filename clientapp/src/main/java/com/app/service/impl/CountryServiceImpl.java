package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.NotFoundException;
import com.app.model.Country;
import com.app.service.CountryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rest клиент
 * реализация CountryService
 */
@Service
@Configuration
@PropertySource("file:./server.conf")
public class CountryServiceImpl implements CountryService {


    @Value("${baseUrl}")
    private String baseUrl;

    private static String allUrl = "/api/countries";
    private static String saveUrl = "/api/country";
    private static String byCodeUrl = "/api/country/{code}";
    private static String deleteUrl = "/api/country/{code}";


    @Autowired
    ApiConfig config;


    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private HttpEntity<String> entityHeaders;
    @Autowired
    private HttpHeaders headers;


    @Autowired
    MultiValueMap<String, String> headersMap;


    /**
     * возвращает список стран
     * @return список стран
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Country> findAll() throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(allUrl));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Country[]> responseEntity = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, entityHeaders, Country[].class);
        Country[] arr = responseEntity.getBody();
        List<Country> countries = new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
            countries.removeIf(c->!c.isVisible());
        return  countries;

    }

    /**
     * возвращает страну по коду
     * @param code код страны
     * @return страна
     * @throws Exception в случае ошибки
     */
    @Override
    public Country findByCode(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byCodeUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("code",code));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        try {
            ResponseEntity<Country> responseEntity = restTemplate
                    .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Country.class);
            Country country = responseEntity.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") &&
                    !country.isVisible())  throw new NotFoundException("Страна не найдена");
                return country;
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.NOT_FOUND.equals(httpClientOrServerExc.getStatusCode())) {
               throw new NotFoundException(String.format("Страна по коду %s не найдена",code));
            }
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
        }catch (Exception e){
            throw new ApiException(e.getMessage());
        }

        // сюда мы не дойдем, но по синтаксису надо что-то вернуть
        throw new ApiException("Неизвестная ошибка");
    }

    /**
     * сохраняет страну
     * @param country страна
     * @return сохраненная страна
     * @throws Exception в случае ошибки
     */
    @Override
    public Country save(Country country) throws Exception {
          UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Country> request = new HttpEntity(country, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Country> response = restTemplate.postForEntity(builder.toUriString(), request, Country.class);
            country = response.getBody();
            // если пользователь и страна не видимая
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !country.isVisible())
                throw  new NotFoundException("Не найдено!");
            return  country;
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return null;

    }

    /**
     * удаляет страну по коду
     * @param code код страны
     * @throws Exception в случае ошибки
     */
    @Override
    public void delete(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(deleteUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("code",code));

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.delete(uri.toString());
            // если сервер вернул ошибку
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если другое исключение
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
