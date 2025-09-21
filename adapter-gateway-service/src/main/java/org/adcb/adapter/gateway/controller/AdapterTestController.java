package org.adcb.adapter.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adcb.adapter.commons.StandardResponse;
import org.adcb.adapter.gateway.service.EnhancedProtocolAdapterService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Test controller for adapter service functionality.
 */
@RestController
@RequestMapping("/adapter")
@RequiredArgsConstructor
@Slf4j
public class AdapterTestController {

    private final EnhancedProtocolAdapterService adapterService;

    /**
     * Test endpoint to call user API via adapter.
     *
     * GET /adapter/test/user/1
     */
    @GetMapping("/test/user/{userId}")
    public Mono<StandardResponse<?>> getUserViaAdapter(@PathVariable String userId) {
        return Mono.fromCallable(() -> adapterService.invoke("user-api", Map.of("userId", userId)))
                .subscribeOn(Schedulers.boundedElastic())
                .map(r -> (StandardResponse<?>) r);  // cast to wildcard type
    }



    /**
     * Test endpoint to call SOAP user-details API via adapter.
     *
     * GET /adapter/test/soap-user/{userId}
     */
    @GetMapping("/test/soap-user/{userId}")
    public Mono<StandardResponse<?>> getSoapUserViaAdapter(@PathVariable String userId) {
        log.info("Testing SOAP adapter with userId: {}", userId);
        Map<String, Object> requestData = Map.of(
                "userId", userId
        );
        return Mono.fromCallable(() ->
                        adapterService.invoke("user-soap-api", requestData)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(r -> (StandardResponse<?>) r);
    }


    /**
     * Generic test endpoint with request body.
     *
     * POST /adapter/test/{serviceName}
     */
    @PostMapping("/test/{serviceName}")
    public StandardResponse<?> testService(@PathVariable String serviceName,
                                           @RequestBody Map<String, Object> requestData) {
        log.info("Testing service '{}' with  {}", serviceName, requestData);
        return adapterService.invoke(serviceName, requestData);
    }

    /**
     * Generic test endpoint with request body for any configured service.
     *
     * POST /adapter/test/{serviceName}
     */
    @PostMapping("/test1/{serviceName}")
    public Mono<StandardResponse<?>> testService1(@PathVariable String serviceName,
                                                 @RequestBody Map<String, Object> requestData) {
        log.info("Testing service '{}' with  {}", serviceName, requestData);
        return Mono.fromCallable(() ->
                        adapterService.invoke(serviceName, requestData)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(r -> (StandardResponse<?>) r);
    }



}
