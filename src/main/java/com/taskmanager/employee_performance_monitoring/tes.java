package com.taskmanager.employee_performance_monitoring.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@RestController
@RequestMapping("/test")
public class tes{

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping
    public String testDB() {
        mongoTemplate.getCollectionNames(); // forces DB connection
        return "MongoDB Connected";
    }
}