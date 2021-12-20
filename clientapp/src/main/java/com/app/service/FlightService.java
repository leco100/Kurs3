package com.app.service;

import com.app.model.Airport;
import com.app.model.Flight;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {
    List<Flight> findByPeriod(LocalDate from,LocalDate to) throws Exception;
    Flight save(Flight flight) throws Exception;
    Flight insert(Flight flight) throws Exception;
    Flight findByCode(String code) throws Exception;
    void delete(String code) throws Exception;
    List<Flight> findByAirportSrcAndDate(Airport airport,LocalDate date);
    List<Flight> findByAirportDstAndDate(Airport airport, LocalDate date);


}
