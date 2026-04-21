package org.example.springbootdemo.service;

import org.example.springbootdemo.model.Cars;
import org.example.springbootdemo.respository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {

    private CarRepository repo;

    @Autowired
    public CarService(CarRepository repo){
        this.repo = repo;
    }

    public Cars addCar(Cars car){
        return repo.save(car);
    }

    public List<Cars> getAllCars(){
        return repo.findAll();
    }

}
