package com.app.service;

import com.app.exception.WrongUserException;
import com.app.model.Users;


public interface UserService {
    Users authUser(String login, String password) throws WrongUserException;
    Users saveUser(Users user) throws RuntimeException;
    Users createUser(Users user) throws RuntimeException;
}
