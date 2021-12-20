package com.app.repos;


import com.app.entity.Airport;
import com.app.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface AirportRepository extends JpaRepository<Airport, String>,
        JpaSpecificationExecutor<String>
{

    List<Airport> findByCountry(Country country);


}
