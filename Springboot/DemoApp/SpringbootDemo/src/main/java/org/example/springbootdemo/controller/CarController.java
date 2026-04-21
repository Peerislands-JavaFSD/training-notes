package org.example.springbootdemo.controller;


import org.example.springbootdemo.model.Cars;
import org.example.springbootdemo.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {

    private CarService service;
    @Autowired
    public CarController(CarService service){
        this.service = service;
    }

    @GetMapping("/view")
    public ResponseEntity<List<Cars>> getAllCars(){
        return new ResponseEntity<>(service.getAllCars(), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Cars> addCar(@RequestBody Cars car){
        return new ResponseEntity<>(service.addCar(car), HttpStatus.CREATED);
    }


}
