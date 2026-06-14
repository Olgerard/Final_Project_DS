package broker.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all HTTP communication with the 3 supplier services.
 *
 * Flow per supplier:
 *   1. GET /{products}/{eventId}  → find available product id
 *   2. POST /reservations          → reserve (phase 1)
 *   3. PUT  /reservations/{id}/confirm or /cancel → phase 2
 */
@Component
public class SupplierClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ACCOMMODATION_URL = "https://eventhub-accommodation-c7ajakacc5h4ggax.polandcentral-01.azurewebsites.net";
    private static final String TICKET_URL        = "https://eventhub-ticket-hgabb0baazhwe3bm.switzerlandnorth-01.azurewebsites.net";
    private static final String TRANSPORT_URL     = "https://transport-supplier-c7cpace0bscvdva2-dyakemfaajceaqbg.swedencentral-01.azurewebsites.net";

    // -----------------------------------------------------------------------
    // Phase 1 — Reserve at each supplier
    // Returns reservation id, or -1 on failure
    // -----------------------------------------------------------------------

    public int reserveAccommodation(int eventId, int accommodationId, int quantity) {
        try {
            return doReserve(ACCOMMODATION_URL, "accommodationId", accommodationId, quantity);
        } catch (Exception e) {
            System.err.println("reserveAccommodation failed: " + e.getMessage());
            return -1;
        }
    }

    public int reserveTicket(int eventId, int ticketId, int quantity) {
        try {
            return doReserve(TICKET_URL, "ticketId", ticketId, quantity);
        } catch (Exception e) {
            System.err.println("reserveTicket failed: " + e.getMessage());
            return -1;
        }
    }

    public int reserveTransport(int eventId, int transportId, int quantity) {
        try {
            return doReserve(TRANSPORT_URL, "transportId", transportId, quantity);
        } catch (Exception e) {
            System.err.println("reserveTransport failed: " + e.getMessage());
            return -1;
        }
    }

    private int doReserve(String baseUrl, String idField, int productId, int quantity) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put(idField, productId);
            body.put("quantity", quantity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/reservations", request, Map.class);

            if (response.getStatusCode() == HttpStatus.CREATED && response.getBody() != null) {
                Object id = response.getBody().get("id");
                if (id instanceof Integer) return (Integer) id;
                if (id instanceof Number)  return ((Number) id).intValue();
            }
            return -1;
        } catch (Exception e) {
            System.err.println("doReserve failed at " + baseUrl + ": " + e.getMessage());
            return -1;
        }
    }

    // -----------------------------------------------------------------------
    // Phase 2a — Confirm
    // -----------------------------------------------------------------------

    public boolean confirmAccommodation(int reservationId) { return doConfirm(ACCOMMODATION_URL, reservationId); }
    public boolean confirmTicket(int reservationId)        { return doConfirm(TICKET_URL, reservationId); }
    public boolean confirmTransport(int reservationId)     { return doConfirm(TRANSPORT_URL, reservationId); }

    private boolean doConfirm(String baseUrl, int reservationId) {
        try {
            restTemplate.put(baseUrl + "/reservations/" + reservationId + "/confirm", null);
            return true;
        } catch (Exception e) {
            System.err.println("Confirm failed at " + baseUrl + ": " + e.getMessage());
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // Phase 2b — Cancel (rollback)
    // -----------------------------------------------------------------------

    public boolean cancelAccommodation(int reservationId) { return doCancel(ACCOMMODATION_URL, reservationId); }
    public boolean cancelTicket(int reservationId)        { return doCancel(TICKET_URL, reservationId); }
    public boolean cancelTransport(int reservationId)     { return doCancel(TRANSPORT_URL, reservationId); }

    private boolean doCancel(String baseUrl, int reservationId) {
        try {
            restTemplate.put(baseUrl + "/reservations/" + reservationId + "/cancel", null);
            return true;
        } catch (Exception e) {
            System.err.println("Cancel failed at " + baseUrl + ": " + e.getMessage());
            return false;
        }
    }

    public List<Map> getAccommodations(int eventId) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    ACCOMMODATION_URL + "/accommodations/" + eventId, List.class);
            return response.getBody() != null ? response.getBody() : List.of();
        }
        catch (Exception e) {
            System.err.println("getAccommodations failed: " + e.getMessage());
            return List.of();
        }
    }

    public List<Map> getTickets(int eventId) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    TICKET_URL + "/tickets/" + eventId, List.class);
            return response.getBody() != null ? response.getBody() : List.of();
        }
        catch (Exception e) {
            System.err.println("getTickets failed: " + e.getMessage());
            return List.of();
        }
    }

    public List<Map> getTransport(int eventId) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    TRANSPORT_URL + "/transport/" + eventId, List.class);
            return response.getBody() != null ? response.getBody() : List.of();
        }
        catch (Exception e) {
            System.err.println("getTransport failed: " + e.getMessage());
            return List.of();
        }
    }
}
