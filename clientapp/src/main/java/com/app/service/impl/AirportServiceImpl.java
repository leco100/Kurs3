package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.NotFoundException;
import com.app.model.Airport;
import com.app.model.Company;
import com.app.service.AirportService;
import com.app.service.CompanyService;
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
 * реализация интферфейса CompanyService
 */
@Service
@Configuration
@PropertySource("file:./server.conf")
public class AirportServiceImpl implements AirportService {


    @Value("${baseUrl}")
    private String baseUrl;

    @Value("${air.code}")
    private String airCode;


    private static String allUrl = "/api/airports";
    private static String saveUrl = "/api/airport";
    private static String byCountryUrl = "/api/airports/{code}";
    private static String byIdUrl = "/api/airport/{code}";
    private static String deleteUrl = "/api/airport/{code}";


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
     * возвращает список аэропортов
     * @return список
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Airport> findAll() throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(allUrl));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Airport[]> responseEntity = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, entityHeaders, Airport[].class);
        Airport[] arr = responseEntity.getBody();
        List<Airport> airports =new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
            airports.removeIf(c->!c.isVisible());
        return  airports;

    }

    /**
     *  возвращает список компаний по коду страны
     * @param code код старны
     * @return список компаний
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Airport> findByCountry(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byCountryUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("code",code));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Airport[]> responseEntity = restTemplate
                .exchange(uri.toUriString(), HttpMethod.GET, entityHeaders, Airport[].class);
        Airport[] arr = responseEntity.getBody();
        List<Airport> airports = new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
            airports.removeIf(c->!c.isVisible());
        return  airports;
    }

    /**
     * возвращает аэропорт по коду
     * @param code аэропорта
     * @return аэрпорт
     * @throws Exception в случае ошибки
     */
    @Override
    public Airport findByCode(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byIdUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("code",code));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Airport> responseEntity = restTemplate
                .exchange(uri.toUriString(), HttpMethod.GET, entityHeaders, Airport.class);
        Airport airport = responseEntity.getBody();
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
        {
            // если пользователь и компания "не видимая" - то выбросим исключение
            if (!airport.isVisible())throw new NotFoundException("Аэропорт не найден!");
        }
        return  airport;
    }


    /**
     * сохраняет аэропорт
     * @param airport аэропорт
     * @return сохраненный аэропорт
     * @throws Exception в случае ошибки
     */
    @Override
    public Airport save(Airport airport) throws Exception {
          UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Airport> request = new HttpEntity(airport, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Airport> response = restTemplate.postForEntity(builder.toUriString(), request, Airport.class);
            airport = response.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !airport.isVisible())
                // если компания "не видимая" - выбросим исключение
                throw  new NotFoundException("Не найдено!");
            return  airport;
            // если сервер прислал ошибку
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если другое исключение
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        throw new NotFoundException();

    }

    /**
     * удаляет аэропорт по коду
     * @param code код аэропорта
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
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Airport homeAirport() {
        try {
            return  findByCode(airCode);
        }catch (Exception e){

        }
        return null;
    }
}
