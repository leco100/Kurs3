package com.app.service;

import com.app.model.Airport;
import com.app.model.Company;

import java.util.List;

public interface AirportService {
    List<Airport> findAll() throws  Exception;
    List<Airport> findByCountry(String code) throws  Exception;
    Airport findByCode(String code) throws  Exception;
    Airport save(Airport airport) throws  Exception;
    void delete(String code) throws  Exception;
    Airport homeAirport();


}
