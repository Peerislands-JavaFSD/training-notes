package org.example.springbootdemo.respository;

import org.example.springbootdemo.model.Cars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Cars, Long> {
}
