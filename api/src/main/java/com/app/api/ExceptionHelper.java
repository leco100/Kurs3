package com.app.api;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * перехватывает ошибки, которые возвращает rest контроллер и формирует ответ, какая ошибка произошла
 * в читаемой форме
 */
@ControllerAdvice
public class ExceptionHelper {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleInvalidInputException(Exception ex) {
        logger.error("Exception: "+ex.getMessage());
        Throwable  e = null;
        String msg = ex.getMessage();
        while (true){
            msg = ex.getMessage();
            e = ex.getCause();
            if (e==null) break;
            msg = e.getMessage();
            System.out.println(msg);
        }
        return new ResponseEntity<Object>(msg, BAD_REQUEST);

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult result = ex.getBindingResult();

        List<String> errorList = new ArrayList<>();
        result.getFieldErrors().forEach((fieldError) -> {
            errorList.add(fieldError.getDefaultMessage() +" : " + fieldError.getRejectedValue());
        });
        result.getGlobalErrors().forEach((fieldError) -> {
            errorList.add(fieldError.getDefaultMessage() );
        });

        //return new Error(HttpStatus.BAD_REQUEST, ex.getMessage(), errorList);
        return new  ResponseEntity<Object>(errorList.stream().collect(Collectors.joining(",")), BAD_REQUEST);
    }



}
