package com.example.service;

import brave.Span;
import brave.Tracer;
import brave.baggage.BaggageField;
import brave.internal.baggage.ExtraBaggageContext;
import com.example.repository.SecondRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class SecondService {


    private static final String thirdUri = "http://localhost:8082/third";
    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final SecondRepository secondRepository;

    public String ping(){
        log.info(">>> second service ... end");
        return "request ping success!!" + secondRepository.findAny();
    }

    public String sendThird() {
        log.info(">>> new span start ... ");
        log.info(">>> secondRepo .... {}", secondRepository.findAny());
        String response = restTemplate.getForObject(thirdUri + "/ping", String.class);
        log.info(">>> from Third-point .... response : {}", response);
        return "finish";
    }

    public String createError() {
        log.info(">>> second service ... ");
        secondRepository.findAny();
        secondRepository.createError();
        return "error!";
    }

}
