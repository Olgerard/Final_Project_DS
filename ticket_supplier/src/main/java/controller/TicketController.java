package ticket_supplier.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import ticket_supplier.service.TicketService;

@RestController
public class TicketController {
    @Autowired
    private TicketService ticketService;


}
