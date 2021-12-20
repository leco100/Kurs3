package com.app.repos;

import com.app.entity.Company;
import com.app.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.stereotype.Repository;


import java.util.List;



@Repository
public interface CompanyRepository extends JpaRepository<Company,Long>,
        JpaSpecificationExecutor<Company>
{

    List<Company> findByCountry(Country country);
}
