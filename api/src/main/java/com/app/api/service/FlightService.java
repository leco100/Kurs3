package com.app.api.service;

import com.app.entity.Airport;
import com.app.entity.Flight;
import com.app.repos.AirportRepository;
import com.app.repos.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * служба AirplaneService
 */
@Service
public class FlightService {
    // возвращает список стран согласно странице и кол-ву на странице

    @Autowired
    FlightRepository repository;

    @Autowired
    AirportRepository airportRepository;


    /**
     * возвращает список полетов за период
     * @param from начало периода
     * @param to конец периода
     * @return список авиарейсов
     */
    public List<Flight> getFlights(LocalDate from,LocalDate to){

        List<Flight> result = repository.findByDateDepBetween(from,to);
        return result;
    }

    /**
     * возвращает расписание по коду
     * @param code код расписания
     * @return Optional
     */
    public Optional<Flight> getFlight(String code)
    {

        return repository.findById(code);
    }

    /**
     * сохраняет расписание в базу
     * @param flight расписание
     * @throws Exception может выбросить Exception если будет ошибка
     * @return  Flight рейс
     */
    @Transactional(rollbackOn = Exception.class)
    public Flight saveFlight(Flight flight) throws Exception{
        Optional<Flight> old = repository.findById(flight.getCode());
        repository.save(flight);
        flight = repository.findById(flight.getCode()).get();

        return flight;

    }


    /**
     * удаляет расписание из базы
     * @param flight расписание
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteFlight(Flight flight) throws Exception{
        repository.delete(flight);
    }

    /**
     * удаляет расписаниние из базы по коду
     * @param code код расписание
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteFlightByCode(String code) throws Exception{
        repository.deleteById(code);
    }


    public List<Flight> findByPeriod(LocalDate from, LocalDate to){
        return repository.findByDateDepBetween(from,to);
    }

    public List<Flight> findByDateAndAirportSrc(LocalDate date, String code) {
        Optional<Airport> airport = airportRepository.findById(code);
        // если не нашли аэропорт - вернем пустой список
        if (!airport.isPresent()) return new ArrayList<>();
        return repository.searchByDateAndAirportSrc(date,airport.get());
    }

    public List<Flight> findByDateAndAirportDst(LocalDate date,String code) {
        Optional<Airport> airport = airportRepository.findById(code);
        // если не нашли аэропорт - вернем пустой список
        if (!airport.isPresent()) return new ArrayList<>();
        return repository.searchByDateAndAirportDst(date,airport.get());
    }
}
