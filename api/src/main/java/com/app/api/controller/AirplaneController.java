package com.app.api.controller;


import com.app.api.service.AirplaneService;
import com.app.entity.Airplane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

/**
 *   RestController, возвращает и принимает данные в форме JSON
 *   Самолеты
 */
@RestController
@Validated
@RequestMapping("/api")
public class AirplaneController {

    // подключим сервис
    @Autowired
    AirplaneService service;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     *  возвращает список самолетов
     */
    @GetMapping(value = "/airplanes")
    public ResponseEntity getAll(HttpServletRequest request){
        logger.info(String.format("call GET\\airplanes  from %s ",request.getRemoteAddr()));
        return ResponseEntity.ok(service.getAirplanes());

    }
    // имеют право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает самолет  по id
     * @param id  id самолета
     */
    @GetMapping(value = "/airplane/{id}")
    ResponseEntity getById(@PathVariable(name="id",required = true) Long id,HttpServletRequest request){
        logger.info(String.format("call GET\\airplane\\%d  from %s ",id,request.getRemoteAddr()));
        Optional<Airplane> airplane = service.getAirplane(id);
        // если нашли - вернем самолет
        if (airplane.isPresent()) return ResponseEntity.ok(airplane.get());
        // если нет - выбросим not found (404)
        return ResponseEntity.notFound().build();
    }
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание самолета в базу
     * @param airplane самолет
     */
    @PostMapping(value = "/airplane")
    ResponseEntity save(@Valid @RequestBody Airplane airplane,HttpServletRequest request){
        logger.info(String.format("call POST\\airplane  from %s ",request.getRemoteAddr()));
        try {
            service.saveAirplane(airplane);
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\airplane  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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

        // если все ок - то 200 код и сохраненний самолет
        return ResponseEntity.ok(airplane);
    }

    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/airplane/{id}")
    ResponseEntity deleteByCode(@PathVariable(name="id",required = true) Long id,HttpServletRequest request){
        logger.info(String.format("call DELETE\\airplane\\%d  from %s ",id,request.getRemoteAddr()));
            try {
                service.deleteAirplaneById(id);
            } catch (Exception e) {
                logger.error(String.format("call DELETE\\airplane\\%d  from %s,%s ",id,request.getRemoteAddr(),e.getMessage()));
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
