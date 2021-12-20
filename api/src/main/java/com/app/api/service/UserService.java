package com.app.api.service;

import com.app.entity.Flight;
import com.app.entity.Users;
import com.app.repos.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UsersRepository repository;

    public Optional<Users> findUserByLogin(String login){
        return repository.findById(login);
    }


    /**
     * сохраняет пользователя в базу
     * @param user пользователь
     * @throws Exception может выбросить Exception если будет ошибка
     */
    @Transactional(rollbackOn = Exception.class)
    public void saveUser(Users user) throws Exception{
        repository.save(user);
    }



}
