package com.app.service;

import com.app.model.Airplane;
import com.app.model.Country;

import java.util.List;

public interface AirplaneService {
    List<Airplane> findAll() throws  Exception;
    Airplane findById(long id) throws  Exception;
    Airplane save(Airplane airplane) throws  Exception;
    void delete(long id) throws  Exception;


}
