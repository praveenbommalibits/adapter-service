/*
package org.adcb.adapter.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.StandardResponse;
import org.adcb.adapter.gateway.service.ProtocolAdapterService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

*/
/**
 * Test controller for adapter service functionality.
 *//*

@RestController
@RequestMapping("/adapter")
@RequiredArgsConstructor
@Slf4j
public class AdapterTestController {

    private final ProtocolAdapterService adapterService;

    */
/**
     * Test endpoint to call user API via adapter.
     *
     * GET /adapter/test/user/1
     *//*

    @GetMapping("/test/user/{userId}")
    public StandardResponse<?> getUserViaAdapter(@PathVariable String userId) {
        log.info("Testing adapter with userId: {}", userId);

        Map<String, Object> requestData = Map.of(
                "userId", userId,
                "operation", "getUserById"
        );

        return adapterService.call("user-api", requestData);
    }

    */
/**
     * Generic test endpoint with request body.
     *
     * POST /adapter/test/{serviceName}
     *//*

    @PostMapping("/test/{serviceName}")
    public StandardResponse<?> testService(@PathVariable String serviceName,
                                           @RequestBody Map<String, Object> requestData) {
        log.info("Testing service '{}' with  {}", serviceName, requestData);
        return adapterService.call(serviceName, requestData);
    }
}
*/
