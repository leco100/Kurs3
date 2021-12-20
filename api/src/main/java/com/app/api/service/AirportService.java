package com.app.api.service;

import com.app.entity.Airport;
import com.app.entity.Country;
import com.app.repos.AirportRepository;
import com.app.repos.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * служба CountryService
 */
@Service
public class AirportService {
    // возвращает список стран согласно странице и кол-ву на странице


    @Autowired
    AirportRepository airportRepository;

    @Autowired
    CountryRepository countryRepository;



    /**
     * возвращает список аэроротов

     * @return список аэропорртов
     */
    public List<Airport> getAirports(){

        List<Airport> result = airportRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return  result;
    }

    /**
     * возвращает аэророрт по коду
     * @param code код аэропорта
     * @return Optional
     */
    public Optional<Airport> getAirport(String code)
    {
        return airportRepository.findById(code);
    }

    /**
     * возвращает аэророрт по коду
     * @param code код аэропорта
     * @return Optional
     */
    public List<Airport> getAirportByCountry(String code)
    {
        Optional<Country> country = countryRepository.findById(code);
        if (!country.isPresent()) return new ArrayList<>();
        return airportRepository.findByCountry(country.get());
    }

    /**
     * сохраняет аэророрт в базу
     * @param airport аэрропорт
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void saveAirport(Airport airport) throws Exception{
        airportRepository.save(airport);
    }


    /**
     * удаляет аэророрт из базы
     *  @param airport аэрропорт
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteAirport(Airport airport) throws Exception{
        airportRepository.delete(airport);
    }

    /**
     * удаляет аэророрт из базы по коду
     * @param code код аэрропорта
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteAirportByCode(String code) throws Exception{
        airportRepository.deleteById(code);
    }
}
