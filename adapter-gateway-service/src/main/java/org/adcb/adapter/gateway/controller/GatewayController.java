package org.adcb.adapter.gateway.controller;

import org.adcb.adapter.gateway.service.ProtocolAdapterService;

import java.util.Collections;
import java.util.Map;

/**
 * REST controller entry point placeholder (non-Spring). Provides a simple handle method.
 */
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/adapter")
@RequiredArgsConstructor
public class GatewayController {

    private final ProtocolAdapterService adapterService;

    @PostMapping("/call/{serviceName}")
    public ResponseEntity<Object> callService(@PathVariable String serviceName,
                                              @RequestBody Map<String, Object> requestData) {
        Object response = adapterService.call(serviceName, requestData);
        return ResponseEntity.ok(response);
    }
}

