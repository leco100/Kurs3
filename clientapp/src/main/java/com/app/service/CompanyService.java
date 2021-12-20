package com.app.service;

import com.app.model.Company;
import com.app.model.Country;

import java.util.List;

public interface CompanyService {
    List<Company> findAll() throws  Exception;
    List<Company> findByCountry(String code) throws  Exception;
    Company findById(long id) throws  Exception;
    Company save(Company company) throws  Exception;
    void delete(long id) throws  Exception;


}
