package com.app.service;

import com.app.model.Country;

import java.util.List;

public interface CountryService {
    List<Country> findAll() throws  Exception;
    Country findByCode(String code) throws  Exception;
    Country save(Country country) throws  Exception;
    void delete(String code) throws  Exception;


}
