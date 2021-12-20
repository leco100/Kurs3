package com.app.repos;

import com.app.entity.Airplane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirplaneRepository extends JpaRepository<Airplane,Long>, JpaSpecificationExecutor<Airplane> {
    Optional<Airplane> findByModel(String model);
}
