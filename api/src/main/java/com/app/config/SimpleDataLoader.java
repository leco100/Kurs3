package com.app.config;

import com.app.entity.*;
import com.app.repos.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * загрузка данных
 */
@Component
public class SimpleDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    ResourceLoader resourceLoader;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UsersRepository userRepository;

    @Autowired
    CountryRepository countryRepository;
    @Autowired
    AirportRepository airportRepository;

    @Autowired
    AirplaneRepository airplaneRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Role user = createRoleIfNotFound("ROLE_USER", "Пользователь");
        Role admin = createRoleIfNotFound("ROLE_ADMIN", "Админ");


        // создадим пользователей
        Users adminUser = createUserifNotFound("admin", "123456", admin);
        Users readerUser = createUserifNotFound("oper", "123456", user);
        loadData();
        loadAirplane();

    }

    @Transactional
    Role createRoleIfNotFound(final String name, final String info) {
        Role role = roleRepository.findById(name).orElse(null);
        if (role == null) {
            role = new Role(name, info);
            role = roleRepository.saveAndFlush(role);
        }
        return role;
    }

    @Transactional
    Users createUserifNotFound(final String login, final String password, Role role) {
        Users user = userRepository.findById(login).orElse(null);
        if (user == null) {
            user = new Users(login, passwordEncoder.encode(password), role);
            user.setFirstName(login);
            user.setLastName(login);
            user.setActive(true);
            user = userRepository.saveAndFlush(user);
        }
        return user;
    }

    // загрузим справочник аэропортов и стран
    @Transactional
    void loadData() {

        try {

            String fileName = System.getProperty("user.dir").concat("/airports.csv");
            File file = new File(fileName);

            Stream<String> lines = Files.lines(file.toPath(), Charset.forName("cp1251"));

            List<Airport> airports = lines.map(l->{
                String[] arr = l.split(";");
                Country country = new Country(arr[6],  arr[4], arr[5] );
                Airport airport = new Airport(arr[0],  arr[1], arr[3], country);
                return airport;
            }).collect(Collectors.toList());

            List<Country> countryStream = airports.stream().map(a->a.getCountry()).distinct().collect(Collectors.toList());
            for(Country country:countryStream){
               if (!countryRepository.findById(country.getCode()).isPresent())
                   countryRepository.saveAndFlush(country);
            }
            for(Airport airport:airports){
                if (!airportRepository.findById(airport.getCode()).isPresent()){
                    if (airport.getCity()==null || airport.getCity().isEmpty())
                        airport.setCity(airport.getName());
                    airportRepository.saveAndFlush(airport);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    // загрузим справочник самолетов

    @Transactional
    private void loadAirplane(){
        List<Airplane> list = Arrays.asList(
                new Airplane("Ту-134", "Туполев Ту-134", 80, 16),
                new Airplane("Ту-154", "Туполев Ту-154", 152, 6),
                new Airplane("Ту-204", "Туполев Ту-204", 196, 18),
                new Airplane("SSJ 100", "Сухой Суперджет-100", 95, 3),
                new Airplane("ИЛ-62", "Ильюшин ИЛ-62", 138, 10),
                new Airplane("ИЛ-86", "Ильюшин ИЛ-86", 234, 80),
                new Airplane("ИЛ-96", "Ильюшин ИЛ-96", 235, 65),
                new Airplane("A310", "Airbus A310", 183, 0),
                new Airplane("A320", "Airbus A320", 150, 10),
                new Airplane("A330", "Airbus A330", 295, 40),
                new Airplane("B737", "Boeing-737", 122, 0),
                new Airplane("B747", "Boeing-747", 266, 76),
                new Airplane("B767", "Boeing-767", 269, 59),
                new Airplane("B777", "Boeing-777", 148, 70));
        for(Airplane airplane:list){
           if (!airplaneRepository.findByModel(airplane.getModel()).isPresent()){
               airplaneRepository.saveAndFlush(airplane);
           }
        }

    }

    /**
     * внутренний класс для загрузки данных
     */
    @Getter
    @Setter
    @Builder
    static class Load {
        String icaoCode; //0
        String nameRus; //1
        String nameEng; //2
        String cityRus; //3
        String countryRus; //4
        String countryEng; //5
        String isoCode; //6

        public Load() {
        }

        public Load(String icaoCode, String nameRus, String nameEng, String cityRus, String countryRus, String countryEng, String isoCode) {
            this.icaoCode = icaoCode;
            this.nameRus = nameRus;
            this.nameEng = nameEng;
            this.cityRus = cityRus;
            this.countryRus = countryRus;
            this.countryEng = countryEng;
            this.isoCode = isoCode;
        }
    }

}
