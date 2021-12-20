package com.app.service.impl;

import com.app.config.ApiConfig;
import com.app.exception.ApiException;
import com.app.exception.NotFoundException;
import com.app.model.Airplane;
import com.app.service.AirplaneService;
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
 * rest клиент
 * реализация интерфейса AirplaneService
 */
@Service
@Configuration
@PropertySource("file:./server.conf")
public class AirplaneServiceImpl implements AirplaneService {


    @Value("${baseUrl}")
    private String baseUrl;

    private static String allUrl = "/api/airplanes";
    private static String saveUrl = "/api/airplane";
    private static String byIdUrl = "/api/airplane/{id}";
    private static String deleteUrl = "/api/airplane/{id}";


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
     * возвращает список самолетов
     * @return список
     * @throws Exception в случее ошибки
     */
    @Override
    public List<Airplane> findAll() throws Exception {
        // сформируем uri
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(allUrl));
        // получим rest клиент
        RestTemplate restTemplate = config.getTemplate();
        // конверт http message
        //Реализация , HttpMessageConverter которая может читать и писать JSON с помощью Джексона
        // 2.x  ObjectMapper .
        // Этот преобразователь можно использовать для привязки к типизированным bean-компонентам или нетипизированным HashMapэкземплярам.
        // По умолчанию этот конвертер поддерживает application/jsonи application/*+json с UTF-8
        // набором символов.
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        // укажем rest клиенту как конвертировать
        restTemplate.setMessageConverters(list);
        // вызовем get метод и укажем что он должен вернуть массив самолетов
        ResponseEntity<Airplane[]> responseEntity = restTemplate
                .exchange(builder.toUriString(), HttpMethod.GET, entityHeaders, Airplane[].class);
        // получим этот массив
        Airplane[] arr = responseEntity.getBody();
        //  преобразуем в список
        List<Airplane> airplanes =new ArrayList<>(Arrays.asList(arr));
        // если читал не админ - то удалим не видимые самолеты
        if (config.getUser().getRole().getName().equals("ROLE_USER") && !airplanes.isEmpty())
            airplanes.removeIf(a->!a.isVisible());
        return  airplanes;

    }

    @Override
    public Airplane findById(long id) throws Exception {
        // сформируем uri
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(byIdUrl));
        UriComponents uri =builder.build();
        // пропишем параметр
        uri = uri.expand(Collections.singletonMap("id",id));
        // получим rest клиент
        RestTemplate restTemplate = config.getTemplate();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(converter);
        restTemplate.setMessageConverters(list);
        // считаем  с сервера и обработаем ошибку
        try {
            ResponseEntity<Airplane> responseEntity = restTemplate
                    .exchange(uri.toString(), HttpMethod.GET, entityHeaders, Airplane.class);
            Airplane airplane = responseEntity.getBody();

            if (config.getUser().getRole().getName().equals("ROLE_USER") && !airplane.isVisible())
                throw  new NotFoundException("Не найдено!");
            return airplane;
            // если сервер  вернул нам NOT_FOUND или BAD_REQUEST то вызовем "наше" исключение
        }catch (HttpClientErrorException | HttpServerErrorException httpClientOrServerExc) {
            if(HttpStatus.NOT_FOUND.equals(httpClientOrServerExc.getStatusCode())) {
               throw new NotFoundException(String.format("Самолет  по id %d не найден",id));
            }
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если ошибка другая
        }catch (Exception e){
            // "то пробросим" её на верх
            throw new RuntimeException(e.getMessage());
        }

        // сюда не дойдет выполнение, но вернуть что-то надо
        throw new ApiException("Не известная ошибка");
    }

    /**
     * сохраняем самолет
     * @param airplane самолет
     * @return сохраненный самолет
     * @throws Exception может вызвать исключение
     */
    @Override
    public Airplane save(Airplane airplane) throws Exception {
          UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl.concat(saveUrl));

        MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
        c.setObjectMapper(objectMapper);
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        list.add(c);
        HttpEntity<Airplane> request = new HttpEntity(airplane, headers);

        try {
            RestTemplate restTemplate = config.getTemplate();
            restTemplate.setMessageConverters(list);
            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
            ResponseEntity<Airplane> response = restTemplate.postForEntity(builder.toUriString(), request, Airplane.class);
            airplane = response.getBody();
            if (config.getUser().getRole().getName().equals("ROLE_USER") && !airplane.isVisible())
                throw  new NotFoundException("Не найдено!");
            return  airplane;
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
     * удаляет самолет по id
     * @param id самолета
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
            // если сервер вернул BAD_REQUEST, значит что-то не так,
            // прочитаем сообщение
            if(HttpStatus.BAD_REQUEST.equals(httpClientOrServerExc.getStatusCode())) {
                throw new ApiException(httpClientOrServerExc.getResponseBodyAsString().replace("400 Bad Request: ",""));
            }
            // если другое исключение
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
