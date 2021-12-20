package com.app.api.service;


import com.app.entity.Airplane;
import com.app.repos.AirplaneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * служба AirplaneService
 * обмен между контроллером и репозиториями
 */
@Service
public class AirplaneService {


    @Autowired
    AirplaneRepository airplaneRepository;

    /**
     * возвращает список самолетов
     * @return список самолетов
     */
    public List<Airplane> getAirplanes(){
        List<Airplane> result = airplaneRepository.findAll();
        return result;
    }

    /**
     * возвращает самолет по коду
     * @param id код самолета
     * @return Optional
     */
    public Optional<Airplane> getAirplane(Long  id)
    {

        return airplaneRepository.findById(id);
    }

    /**
     * сохраняет самолет в базу
     * @param airplane самолет
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void saveAirplane(Airplane airplane) throws Exception{
        airplaneRepository.save(airplane);
    }


    /**
     * удаляет самолет из базы
     * @param airplane самолет
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteAirplane(Airplane airplane) throws Exception{
        airplaneRepository.delete(airplane);
    }

    /**
     * удаляет самолет из базы по коду
     * @param id код самолета
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void deleteAirplaneById(Long id) throws Exception{
        airplaneRepository.deleteById(id);
    }
}
