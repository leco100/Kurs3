package com.app.api.service;


import com.app.entity.Company;
import com.app.entity.Country;
import com.app.repos.CompanyRepository;
import com.app.repos.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * служба CompanyService
 */
@Service
public class CompanyService {



    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CountryRepository countryRepository;

    /**
     * возвращает список компаний  постранично
     * @return список компаний
     */
    public List<Company> getCompanies(){

        List<Company> result = companyRepository.findAll();
        return result;
    }

    /**
     * возвращает компанию  по id
     * @param id id
     * @return Optional
     */
    public Optional<Company> getCompany(long id)
    {

        return companyRepository.findById(id);
    }

    /**
     * возвращает компании  по по коду страны
     * @param code код страны
     * @return Optional
     */
    public List<Company> getCompaniesByCode(String code)
    {
        Optional<Country> country = countryRepository.findById(code);
        if (!country.isPresent()) return new ArrayList<>();
        return companyRepository.findByCountry(country.get());
    }

    /**
     * сохраняет компанию в базу
     * @param company компания
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void saveCompany(Company company) throws Exception{
        companyRepository.save(company);
    }


    /**
     * удаляет компанию  из базы
     * @param company компания
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteCompany(Company company) throws Exception{
        companyRepository.delete(company);
    }

    /**
     * удаляет компанию из базы по id
     * @param id id
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteCompanyById(long id) throws Exception{
        companyRepository.deleteById(id);
    }
}
