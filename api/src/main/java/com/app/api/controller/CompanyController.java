package com.app.api.controller;


import com.app.api.service.CompanyService;
import com.app.api.service.CountryService;
import com.app.entity.Company;
import com.app.entity.Country;
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
 * rest контроллер Компании
 */
@RestController
@Validated
@RequestMapping("/api")
public class CompanyController {

    // подключим слубжу
    @Autowired
    CompanyService service;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     *  возвразщает список компаний
     */
    @GetMapping(value = "/companies")
    public ResponseEntity getAll(HttpServletRequest request){
        logger.info(String.format("call GET\\companies  from %s ",request.getRemoteAddr()));
        return ResponseEntity.ok(service.getCompanies());

    }

    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает список компании  по коду страны
     * @param code код страны
     */
    @GetMapping(value = "/companies/{code}")
    ResponseEntity getByCountriesCode(@PathVariable(name="code",required = true) String code,HttpServletRequest request){
        logger.info(String.format("call GET\\by country\\%s  from %s ",code,request.getRemoteAddr()));
        List<Company> companies = service.getCompaniesByCode(code);
        // если нашли - вернем компанию
        return ResponseEntity.ok(companies);
    }


    // имею право читать и админ и пользователь
    @PreAuthorize("hasAnyRole('ROLE_USER,ROLE_ADMIN')")
    /**
     * возвращает компанию  по id
     * @param id id компании
     */
    @GetMapping(value = "/company/{id}")
    ResponseEntity getById(@PathVariable(name="id",required = true) Long id,HttpServletRequest request){
        logger.info(String.format("call GET\\company\\%d  from %s ",id,request.getRemoteAddr()));
        Optional<Company> company = service.getCompany(id);
        // если нашли - вернем компанию
        if (company.isPresent()) return ResponseEntity.ok(company.get());
        // если нет - выбросим not found (404)
        return ResponseEntity.notFound().build();
    }
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание компании в базу
     * @param company компания
     */
    @PostMapping(value = "/company")
    ResponseEntity save(@Valid @RequestBody Company company,HttpServletRequest request){
        logger.info(String.format("call POST\\company  from %s ",request.getRemoteAddr()));
        try {
            service.saveCompany(company);
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\company  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
            // вернем сообщение об ошибке
            e.printStackTrace();
            System.out.println(e.getMessage());
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

        // если все ок - то 200 код и сохраненную компанию
        return ResponseEntity.ok(company);
    }

    /**
     * удаление компании с базы
     * @param id компнии
     * @return статус 200 если все ок
     */
    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping(value = "/company/{id}")
    ResponseEntity deleteById(@PathVariable(name="id",required = true) Long id,HttpServletRequest request){
        logger.info(String.format("call DELETE\\company\\%d  from %s ",id,request.getRemoteAddr()));
            try {
                service.deleteCompanyById(id);
            } catch (Exception e) {
                logger.error(String.format("call DELETE\\company\\%d  from %s,%s ",id,request.getRemoteAddr(),e.getMessage()));
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
