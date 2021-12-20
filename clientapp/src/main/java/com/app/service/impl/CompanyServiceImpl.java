package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.NotFoundException;
import com.app.model.Company;
import com.app.model.Country;
import com.app.service.CompanyService;
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
 * реализация интферфейса CompanyService
 */
@Service
@Configuration
@PropertySource("file:./server.conf")
public class CompanyServiceImpl implements CompanyService {


    @Value("${baseUrl}")
    private String baseUrl;

    private static String allUrl = "/api/companies";
    private static String saveUrl = "/api/company";
    private static String byCountryUrl = "/api/companies/{code}";
    private static String byIdUrl = "/api/company/{id}";
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
     * возвращает список компаний
     * @return список
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Company> findAll() throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(allUrl));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Company[]> responseEntity = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, entityHeaders, Company[].class);
        Company[] arr = responseEntity.getBody();
        List<Company> countries =new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
            countries.removeIf(c->!c.isVisible());
        return  countries;

    }

    /**
     *  возвращает список компаний по коду страны
     * @param code код старны
     * @return список компаний
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Company> findByCountry(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byCountryUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("code",code));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Company[]> responseEntity = restTemplate
                .exchange(uri.toUriString(), HttpMethod.GET, entityHeaders, Company[].class);
        Company[] arr = responseEntity.getBody();
        List<Company> countries = new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
            countries.removeIf(c->!c.isVisible());
        return  countries;
    }

    /**
     * возвращает компанию по id
     * @param id компании
     * @return компания
     * @throws Exception в случае ошибки
     */
    @Override
    public Company findById(long id) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byIdUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("id",id));
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        ResponseEntity<Company> responseEntity = restTemplate
                .exchange(uri.toUriString(), HttpMethod.GET, entityHeaders, Company.class);
        Company company = responseEntity.getBody();
        if (config.getUser().getRole().getName().equals("ROLE_USER"))
        {
            // если пользователь и компания "не видимая" - то выбросим исключение
            if (!company.isVisible())throw new NotFoundException("Компания не найдена!");
        }
        return  company;
    }


    /**
     * сохраняет компанию
     * @param company компания
     * @return сохраненную компанию
     * @throws Exception в случае ошибки
     */
    @Override
    public Company save(Company company) throws Exception {
          UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Company> request = new HttpEntity(company, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Company> response = restTemplate.postForEntity(builder.toUriString(), request, Company.class);
            company = response.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !company.isVisible())
                // если компания "не видимая" - выбросим исключение
                throw  new NotFoundException("Не найдено!");
            return  company;
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
     * удаляет компанию по id
     * @param id компании
     * @throws Exception в случае ошибки
     */
    @Override
    public void delete(long id) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(deleteUrl));
        UriComponents uri =builder.build();
        uri = uri.expand(Collections.singletonMap("id",id));

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
}
