package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.NotFoundException;
import com.app.model.Airplane;
import com.app.model.Airport;
import com.app.model.Flight;
import com.app.model.Users;
import com.app.service.AirplaneService;
import com.app.service.FlightService;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * rest клиент
 * реализация интерфейса AirplaneService
 */
@Service
@Configuration
@PropertySource("file:./server.conf")
public class FligthServiceImpl implements FlightService {


    @Value("${baseUrl}")
    private String baseUrl;

    private static String saveUrl = "/api/flight";
    private static String createUrl = "/api/flight/create";
    private static String byPeriodUrl = "/api//flights/{from}/{to}";
    private static String byCodeUrl = "/api/flight/{code}";
    private static String byAirSrcUrl = "/api/flights/{code}/{date}/src";
    private static String byAirDstUrl = "/api//flights/{code}/{date}/dst";


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
     * возвращает список рейсов
     * @return список
     * @throws Exception в случае ошибки
     */
    @Override
    public List<Flight> findByPeriod(LocalDate from,LocalDate to) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byPeriodUrl));
        UriComponents uri =builder.build();
        Map<String,LocalDate> params  = new HashMap();
        params.put("from",from);
        params.put("to",to);
        uri = uri.expand(params);

        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);


        ResponseEntity<Flight[]> responseEntity = restTemplate
                .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Flight[].class);
        Flight[] arr = responseEntity.getBody();
        List<Flight> flights =new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER") && !flights.isEmpty())
            flights.removeIf(f->!f.isVisible());
        return  flights;

    }

    @Override
    public Flight findByCode(String code) throws Exception {
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
            ResponseEntity<Flight> responseEntity = restTemplate
                    .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Flight.class);
            Flight flight = responseEntity.getBody();

            if (config.getUser().getRole().getName().equals("ROLE_USER") && !flight.isVisible())
                throw  new NotFoundException("Не найдено!");
            return flight;
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.NOT_FOUND.equals(httpClientOrServerExc.getStatusCode())) {
               throw new NotFoundException(String.format("Рейс   по коду %s не найден",code));
            }
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }

        throw new ApiException("Не известная ошибка");
    }

    /**
     * сохраняем рейс
     * @param flight рейс
     * @return сохраненный рейс
     * @throws Exception может вызвать исключение
     */
    @Override
    public Flight save(Flight flight) throws Exception {
          UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Flight> request = new HttpEntity(flight, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Flight> response = restTemplate.postForEntity(builder.toUriString(), request, Flight.class);
            flight = response.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !flight.isVisible())
                throw  new NotFoundException("Не найдено!");
            return  flight;
            // если сервер вернул ошибку
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если другое исключение
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        throw new ApiException("Не известная ошибка");
    }

    @Override
    public Flight insert(Flight flight) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(createUrl));
        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Flight> request = new HttpEntity(flight, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Flight> response = restTemplate.postForEntity(builder.toUriString(), request, Flight.class);
            flight = response.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !flight.isVisible())
                throw  new NotFoundException("Не найдено!");
            return  flight;
            // если сервер вернул ошибку
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если другое исключение
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        throw new ApiException("Не известная ошибка");
    }

    /**
     * удаляет рейс по коду
     * @param code  код рейса
     * @throws Exception в случае ошибки
     */
    @Override
    public void delete(String code) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byCodeUrl));
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
    public List<Flight> findByAirportSrcAndDate(Airport airport, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byAirSrcUrl));
        UriComponents uri =builder.build();
        Map<String,String> params  = new HashMap();
        params.put("code",airport.getCode());
        params.put("date",date.format(formatter));
        uri = uri.expand(params);

        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);


        ResponseEntity<Flight[]> responseEntity = restTemplate
                .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Flight[].class);
        Flight[] arr = responseEntity.getBody();
        List<Flight> flights =new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER") && !flights.isEmpty())
            flights.removeIf(f->!f.isVisible());
        return  flights;
    }

    @Override
    public List<Flight> findByAirportDstAndDate(Airport airport, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byAirDstUrl));
        UriComponents uri =builder.build();
        Map<String,String> params  = new HashMap();
        params.put("code",airport.getCode());
        params.put("date",date.format(formatter));
        uri = uri.expand(params);

        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);


        ResponseEntity<Flight[]> responseEntity = restTemplate
                .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Flight[].class);
        Flight[] arr = responseEntity.getBody();
        List<Flight> flights =new ArrayList<>(Arrays.asList(arr));
        if (config.getUser().getRole().getName().equals("ROLE_USER") && !flights.isEmpty())
            flights.removeIf(f->!f.isVisible());
        return  flights;
    }


}
