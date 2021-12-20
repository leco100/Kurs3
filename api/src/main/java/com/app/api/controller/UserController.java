package com.app.api.controller;

import com.app.api.service.UserService;
import com.app.entity.Role;
import com.app.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * rest контроллер пользователей
 */
@RestController
@Validated
@RequestMapping("/api")
public class UserController {


    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService service;

    @Autowired
    PasswordEncoder passwordEncoder;

    // любой може вызвать метод
    /**
     * возвращает пользователя, если такой есть и пароль совпал
     * @param login логин
     * @param password пароль
     */
    @PostMapping(value = "/user/auth")
    ResponseEntity auth(@NotNull String login,  @NotNull String password, HttpServletRequest request){
        logger.info(String.format("call POST\\auth users  %s %s", login,
                request.getRemoteAddr()));
        try {

            Optional<Users> user = service.findUserByLogin(login);
            if (user.isPresent() ){
                // если пользователь заблокирован
                if (!user.get().isActive()){
                    return ResponseEntity.badRequest().body("Пользователь заблокирован!");
                }
                if (passwordEncoder.matches(password, user.get().getPassword())){
                    return ResponseEntity.ok(user.get());
                }
            }

            // если не угадали пароль
            return ResponseEntity.badRequest().body("Не верный логин или пароль!");
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\flight  from %s ,%s ",request.getRemoteAddr(),e.getMessage()));
            // вернем сообщение об ошибке
            if (e.getCause()!=null)
                if (e.getCause().getCause()!=null)
                {
                    String[] message = e.getCause().getCause().getMessage().split(":");
                    String msg=e.getCause().getCause().getMessage();
                    if (message.length>0) msg = message[message.length-1];
                    return ResponseEntity.badRequest().body(e.getMessage());

                }
            return ResponseEntity.badRequest().body(e.getCause().getMessage());
        }

    }


    /**
     * сохрание пользователя в базу
     */
    @PostMapping(value = "/user/create")
    ResponseEntity create(@NotNull String login,  @NotNull String password,
                          @NotNull String firstName,  @NotNull String lastName,@NotNull String role,
                          HttpServletRequest request){
        logger.info(String.format("call POST\\create user  from %s  %s", login,request.getRemoteAddr()));
        Users user = null;
        try {

            Optional<Users> old = service.findUserByLogin(login);
            if (old.isPresent()){
                System.out.println("Данный логин занят");
                  return ResponseEntity.badRequest().body("Данный логин занят");}
            user = new Users();
            user.setLastName(lastName);
            user.setFirstName(firstName);
            user.setLogin(login);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(new Role(role));
            user.setActive(true);
            service.saveUser(user);
        }
        catch (Exception e){
            // если ошибка - выбросим 403 код и ошибку
            logger.error(String.format("call POST\\create user  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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
        return ResponseEntity.ok(user);
    }

    // имеет право писать только админ
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    /**
     * сохрание пользователя в базу
     * @param airplane самолет
     */
    @PostMapping(value = "/user")
    ResponseEntity save(@Valid @RequestBody Users user, HttpServletRequest request){
        logger.info(String.format("call POST\\user  from %s  %s", user.getLogin(),request.getRemoteAddr()));
        try {
            service.saveUser(user);
        }
        catch (Exception e){
            // если ошибка - выбросим 500 код и ошибку
            logger.error(String.format("call POST\\user  from %s,%s ",request.getRemoteAddr(),e.getMessage()));
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
        return ResponseEntity.ok(user);
    }

}
