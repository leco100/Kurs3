package com.app.api.controller;


import com.app.api.service.AirportService;
import com.app.api.service.AirportService;
import com.app.entity.Airport;
import com.app.entity.Airport;
import com.app.entity.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import java.util.List;
import java.util.Optional;

/**
 * RestController Airport
 * возврщает и пишет аэророрты в базу
 */

@RestController
@Validated
@RequestMapping("/api")
public class AirportController {

    // подключим слубжу
    @Autowired
    AirportService service;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     *  возвразщает список  аэропортов
     */
    @GetMapping(value = "/airports")
    public ResponseEntity getAll(    HttpServletRequest request){
        logger.info(String.format("call GET\\airports  from %s ",request.getRemoteAddr()));
        return ResponseEntity.ok(service.getAirports());

    }
    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает аэропорт по коду
     * @paraam code  код аэропорта
     */
    @GetMapping(value = "/airport/{code}")
    ResponseEntity getById(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call GET\\airport\\%s  from %s ",code,request.getRemoteAddr()));
        Optional<Airport> Airport = service.getAirport(code);
        // если нашли - вернем страну
        if (Airport.isPresent()) return ResponseEntity.ok(Airport.get());
        // если нет - выбросим not found (404)
        return ResponseEntity.notFound().build();
    }


    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает аэропорты  по коду страны
     * @param code код страны
     */
    @GetMapping(value = "/airports/{code}")
    ResponseEntity getByCountryCode(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call GET\\by airports by country\\%s  from %s ",code,request.getRemoteAddr()));
        List<Airport> airports = service.getAirportByCountry(code);
        return ResponseEntity.ok(airports);
    }

    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание аэропорта в базу
     * @param airport аэропорт
     */
    @PostMapping(value = "/airport")
    ResponseEntity save(@Valid @RequestBody Airport airport,HttpServletRequest request){
        logger.info(String.format("call POST\\airport  from %s ",request.getRemoteAddr()));
        try {
            service.saveAirport(airport);
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\airport  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
            // вернем сообщение об ошибке
            if (e.getCause()!=null)
                if (e.getCause().getCause()!=null)
                {
                    String[] message = e.getCause().getCause().getMessage().split(":");
                    String msg=e.getCause().getCause().getMessage();
                    if (message.length>0) msg = message[message.length-1];
                    return ResponseEntity.badRequest().body(msg);

                }
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }

        // если все ок - то 200 код и сохраненний аэророрт
        return ResponseEntity.ok(airport);
    }

    // имеет право писать только админ

    /**
     * удаление аэропорта с базы по коду
     * @param code код аэропорта
     * @return 200 статус, если ок, иначе 403 с ошибкой
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/airport/{code}")
    ResponseEntity deleteByCode(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call DELETE\\airport\\%s  from %s ",code,request.getRemoteAddr()));
            try {
                service.deleteAirportByCode(code);
            } catch (Exception e) {
                logger.error(String.format("call DELETE\\airport\\%s  from %s,%s ",code,request.getRemoteAddr(),e.getMessage()));
                // вернем сообщение об ошибке
                if (e.getCause()!=null)
                    if (e.getCause().getCause()!=null)
                    {
                        String[] message = e.getCause().getCause().getMessage().split(":");
                        String msg=e.getCause().getCause().getMessage();
                        if (message.length>0) msg = message[message.length-1];
                        return ResponseEntity.badRequest().body(msg);

                    }
                 return ResponseEntity.badRequest().body(e.getMessage());
            }
        return ResponseEntity.ok().build();

    }
}
