package com.app.api.controller;



import com.app.api.service.FlightService;
import com.app.entity.Flight;

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
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * rest контроллер авиарейсов
 *
 */
@RestController
@Validated
@RequestMapping("/api")
public class FlightController {

    // подключим слубжу
    @Autowired
    FlightService service;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     *  возвразщает список рейсов
     */
    @GetMapping(value = "/flights/{from}/{to}")
    public ResponseEntity getAll(@NotNull @PathVariable LocalDate from,@NotNull @PathVariable LocalDate to,
                                 HttpServletRequest request){
        logger.info(String.format("call GET\\flights  from %s ",request.getRemoteAddr()));
        return ResponseEntity.ok(service.getFlights(from,to));

    }
    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает расписание  по коду аэропорта отлета за указаную дату
     * @param code  code расписания
     */
    @GetMapping(value = "/flights/{code}/{date}/src")
    ResponseEntity getByDateAndAirSrc(@PathVariable(name="code",required = true) String code,
                                      @PathVariable(name="date",required = true) LocalDate date,
                                      HttpServletRequest request){
        logger.info(String.format("call GET\\flight\\%s  from %s ",code,request.getRemoteAddr()));
        List<Flight> flights = service.findByDateAndAirportSrc(date,code);
        // вернем что нашли
        return ResponseEntity.ok(flights);


    }

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает расписание  по коду аэропорта прилета за указаную дату
     * @param code  code расписания
     */
    @GetMapping(value = "/flights/{code}/{date}/dst")
    ResponseEntity getByDateAndAirDst(@PathVariable(name="code",required = true) String code,
                                      @PathVariable(name="date",required = true) LocalDate date,
                                      HttpServletRequest request){
        logger.info(String.format("call GET\\flight\\%s  from %s ",code,request.getRemoteAddr()));
        List<Flight> flights = service.findByDateAndAirportDst(date,code);
        // вернем что нашли
        return ResponseEntity.ok(flights);


    }
    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает расписание   по коду
     * @param code  code расписания
     */
    @GetMapping(value = "/flight/{code}")
    ResponseEntity getById(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call GET\\flight\\%s  from %s ",code,request.getRemoteAddr()));
        Optional<Flight> flight = service.getFlight(code);
        // если нашли - вернем расписание
        if (flight.isPresent()) return ResponseEntity.ok(flight.get());
        // если нет - выбросим not found (404)
        return ResponseEntity.notFound().build();
    }
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * добавление рейса в базу
     * @param flight рейс
     */
    @PostMapping(value = "/flight/create")
    ResponseEntity save(@Valid @RequestBody Flight flight,HttpServletRequest request){
        logger.info(String.format("call POST\\flight  from %s ",request.getRemoteAddr()));
        try {
            Optional<Flight> old = service.getFlight(flight.getCode());
            if (old.isPresent())  return ResponseEntity.badRequest().body("Рейс "+ flight.getCode()+" уже существует!");
            flight = service.saveFlight(flight);
        }
        catch (Exception e){
            e.printStackTrace();
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\flight  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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
        return ResponseEntity.ok(flight);
    }

    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание рейса в базу
     * @param flight рейс
     */
    @PostMapping(value = "/flight")
    ResponseEntity update(@Valid @RequestBody Flight flight,HttpServletRequest request){
        logger.info(String.format("call POST\\flight  from %s ",request.getRemoteAddr()));
        try {
            flight = service.saveFlight(flight);
        }
        catch (Exception e){
            e.printStackTrace();
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\flight  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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
        return ResponseEntity.ok(flight);
    }

    /**
     * удаление рейса с базы
     * @param code код рейс
     * @return 200 если ок
     */
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/flight/{code}")
    ResponseEntity deleteByCode(@PathVariable(name="code",required = true) String  code,HttpServletRequest request){
        logger.info(String.format("call DELETE\\flight\\%s  from %s ",code,request.getRemoteAddr()));
            try {
                service.deleteFlightByCode(code);
            } catch (Exception e) {
                logger.error(String.format("call DELETE\\flight\\%s  from %s,%s ",code,request.getRemoteAddr(),e.getMessage()));
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
