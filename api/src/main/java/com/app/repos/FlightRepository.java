package com.app.repos;

import com.app.entity.Airport;
import com.app.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight,String>, JpaSpecificationExecutor<Flight> {

    // возращает список полетов за период
    @Query("select f from Flight f where f.dateDep between :from and :to or f.dateArrival between :from and :to")
    List<Flight> findByDateDepBetween(LocalDate from, LocalDate to);

    @Query("select f from Flight f where f.dateDep = :date  and f.areaSrc =:from")
    List<Flight> searchByDateAndAirportSrc(LocalDate date, Airport  from);

    @Query("select f from Flight f where f.dateArrival = :date and f.areaDestination =:to")
    List<Flight> searchByDateAndAirportDst(LocalDate date, Airport  to);


}
