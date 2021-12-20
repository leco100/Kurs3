package com.app.api.controller;






import com.app.api.service.CountryService;
import com.app.entity.Country;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;


import java.util.Optional;

/**
 * rest контроллер Страны
 */
@RestController
@Validated
@RequestMapping("/api")
public class CountryController {

    // подключим слубжу
    @Autowired
    CountryService service;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     *  возвразщает списокстран
     *  */
    @GetMapping(value = "/countries")
    public ResponseEntity getAll(
                                 HttpServletRequest request){
        logger.info(String.format("call GET\\countries  from %s ",request.getRemoteAddr()));
        return ResponseEntity.ok(service.getCountries());

    }
    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает страну  по коду
     * @paraam code  код страны
     */
    @GetMapping(value = "/country/{code}")
    ResponseEntity getById(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call GET\\country\\%s  from %s ",code,request.getRemoteAddr()));
        Optional<Country> country = service.getCountry(code);
        // если нашли - вернем страну
        if (country.isPresent()) return ResponseEntity.ok(country.get());
        // если нет - выбросим not found (404)
        return ResponseEntity.notFound().build();
    }
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание страны в базу
     * @param country страна
     */
    @PostMapping(value = "/country")
    ResponseEntity save(@Valid @RequestBody Country country,HttpServletRequest request){
        logger.info(String.format("call POST\\country  from %s ",request.getRemoteAddr()));
        try {
            service.saveCountry(country);
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\country  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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

        // если все ок - то 200 код и сохраненную страну
        return ResponseEntity.ok(country);
    }

    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/country/{code}")
    ResponseEntity deleteByCode(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call DELETE\\country\\%s  from %s ",code,request.getRemoteAddr()));
            try {
                service.deleteCountryByCode(code);
            } catch (Exception e) {
                logger.error(String.format("call DELETE\\country\\%s  from %s,%s ",code,request.getRemoteAddr(),e.getMessage()));
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
