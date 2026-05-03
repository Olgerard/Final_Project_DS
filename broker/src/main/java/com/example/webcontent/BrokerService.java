package com.example.webcontent;

import org.springframework.stereotype.Service;

@Service
public class BrokerService{
    public String getStatus(){
        return "Service works";
    }
}