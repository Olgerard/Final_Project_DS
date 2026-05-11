package transport_supplier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import transport_supplier.service.TransportService;

@RestController
public class TransportController {
    @Autowired
    private TransportService transportService;
}
