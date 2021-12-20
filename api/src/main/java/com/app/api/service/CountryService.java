package com.app.api.service;

import com.app.entity.Country;
import com.app.repos.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * служба CountryService
 */
@Service
public class CountryService {
    // возвращает список стран согласно странице и кол-ву на странице

    @Autowired
    CountryRepository countryRepository;

    /**
     * возвращает списко стран

     * @return список стран
     */
    public List<Country> getCountries(){

        List<Country> result = countryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return  result;
    }

    /**
     * возвращает страну по коду
     * @param code код страны
     * @return Optional
     */
    public Optional<Country> getCountry(String code)
    {
        return countryRepository.findById(code);
    }

    /**
     * сохраняет страну в базу
     * @param country страна
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void saveCountry(Country country) throws Exception{
        countryRepository.save(country);
    }


    /**
     * удаляет страну из базы
     * @param country страны
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteCountry(Country country) throws Exception{
        countryRepository.delete(country);
    }

    /**
     * удаляет страну из базы по коду
     * @param code код страны
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteCountryByCode(String code) throws Exception{
        countryRepository.deleteById(code);
    }
}
