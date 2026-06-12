package com.bank.islamic.musharakahpartnershipfinancing.api;

import com.bank.islamic.musharakahpartnershipfinancing.model.ControlRecord;
import com.bank.islamic.musharakahpartnershipfinancing.service.ControlRecordStore;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

/**
 * BIAN semantic API for the "Musharakah Partnership Financing" service domain.
 *
 * Endpoints follow the BIAN action-term style:
 *   GET  /v1/service-domain                          → who am I (SD metadata)
 *   POST /v1/musharakah-facility-fulfillment-arrangement/initiate                    → Initiate a control record
 *   GET  /v1/musharakah-facility-fulfillment-arrangement                             → Retrieve (list)
 *   GET  /v1/musharakah-facility-fulfillment-arrangement/{crId}/retrieve             → Retrieve (single)
 *   PUT  /v1/musharakah-facility-fulfillment-arrangement/{crId}/update               → Update
 *   PUT  /v1/musharakah-facility-fulfillment-arrangement/{crId}/control              → Control (suspend|resume|terminate)
 */
@RestController
@RequestMapping("/v1")
public class ServiceDomainController {

    private final ControlRecordStore store;

    public ServiceDomainController(ControlRecordStore store) {
        this.store = store;
    }

    @GetMapping("/service-domain")
    public Map<String, String> serviceDomain() {
        return Map.of(
                "serviceDomain", "Musharakah Partnership Financing",
                "businessArea", "Islamic Financing",
                "businessDomain", "Lease And Partnership Financing",
                "functionalPattern", "Fulfill",
                "assetType", "Musharakah Facility",
                "controlRecord", "Musharakah Facility Fulfillment Arrangement",
                "version", "0.1.0",
                "phase", "1-shallow"
        );
    }

    @PostMapping("/musharakah-facility-fulfillment-arrangement/initiate")
    @CircuitBreaker(name = "serviceDomain")
    public ResponseEntity<ControlRecord> initiate(@RequestBody(required = false) Map<String, Object> properties) {
        return ResponseEntity.status(HttpStatus.CREATED).body(store.initiate(properties));
    }

    @GetMapping("/musharakah-facility-fulfillment-arrangement")
    public Collection<ControlRecord> list() {
        return store.list();
    }

    @GetMapping("/musharakah-facility-fulfillment-arrangement/{crId}/retrieve")
    public ResponseEntity<ControlRecord> retrieve(@PathVariable String crId) {
        return store.retrieve(crId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/musharakah-facility-fulfillment-arrangement/{crId}/update")
    public ResponseEntity<ControlRecord> update(@PathVariable String crId,
                                                @RequestBody Map<String, Object> properties) {
        return store.update(crId, properties)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/musharakah-facility-fulfillment-arrangement/{crId}/control")
    public ResponseEntity<?> control(@PathVariable String crId,
                                     @RequestBody Map<String, String> body) {
        try {
            return store.control(crId, body.get("action"))
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
