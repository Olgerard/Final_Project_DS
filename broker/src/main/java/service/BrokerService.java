package service;

import domain.Order;
import domain.OrderRepository;
import domain.ReservationStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class BrokerService{
    @Autowired
    private OrderRepository orderRepository;
}