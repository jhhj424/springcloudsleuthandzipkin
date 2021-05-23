package com.example.service;

import brave.*;
import brave.baggage.BaggageField;
import brave.baggage.BaggagePropagation;
import brave.baggage.BaggagePropagationConfig;
import brave.internal.baggage.ExtraBaggageContext;
import brave.propagation.B3Propagation;
import brave.sampler.SamplerFunction;
import com.example.repository.FirstRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class FirstService {

    private static final String secondUri = "http://localhost:8081/second";
    private static final String thirdUri = "http://localhost:8082/third";
    private final RestTemplate restTemplate;
    private final Tracer tracer;
    private final CurrentSpanCustomizer currentSpanCustomizer;
    private final FirstRepository firstRepository;

    public String sendSecond() {
        log.info(">>> new span start ... ");
        String response = restTemplate.getForObject(secondUri + "/ping", String.class);
        log.info(">>> from second-point .... response : {}", response);
        log.info(">>> new span start ... ");
        response = restTemplate.getForObject(thirdUri + "/ping", String.class);
        log.info(">>> from second-point .... response : {}", response);
        return "finish";
    }

    public String sendSecondAndThird() {
        firstRepository.findAny();
        log.info(">>> new span start ... ");
        String response = restTemplate.getForObject(secondUri + "/send-third", String.class);
        log.info(">>> from second-point .... response : {}", response);

        log.info(">>> new span start 2 ... ");
        response = restTemplate.getForObject(secondUri + "/send-third", String.class);
        log.info(">>> from second-point 2 .... response : {}", response);

        log.info(">>> new span start 3 ... ");
        response = restTemplate.getForObject(thirdUri + "/ping", String.class);
        log.info(">>> from third-point .... response : {}", response);

        return "finish";
    }

    public String createNewTracer(){
        log.info(">>> first service");
        Span newSpan = tracer.newTrace().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            log.info(">>> new span start with new tracer ");
            String response = restTemplate.getForObject(secondUri + "/ping", String.class);
            log.info(">>> from second-point .... response : {}", response);
            return "finish";
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            newSpan.finish();
        }
        return "error!!";
    }

    public void sendSecondForErrorTest() {
        log.info(">>> first service");
        Span nextSpan = tracer.nextSpan();
        log.info(">>> create new span");
        tracer.withSpanInScope(nextSpan);
        log.info(">>> create new span in scope");
        restTemplate.getForObject(secondUri + "/error", String.class);
    }

    public void addSpan() {
        log.info(">>> first service ... ");

        Span newSpan = tracer.newTrace().name("newSpan").start();
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(newSpan.start())) {
            log.info(">>> new span start ... ");
        } finally {
            newSpan.finish();
        }
    }

    public void nextSpan() {
        log.info(">>> first service ... ");
        SpanCustomizer spanCustomizer = tracer.nextSpan();
        spanCustomizer.name("my new span");
        Span nextSpan = (Span) spanCustomizer;
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(nextSpan.start())) {
            currentSpanCustomizer.tag("customizer", "true");
            log.info(">>> next span start ... ");
        } finally {
            nextSpan.finish();
        }
    }

    @NewSpan("addTagSpan")
    public void addTag(@SpanTag(key = "zeroTag", expression = "'hello characters'") String tag) {
        log.info(">>> first service ... ");
        Span span = tracer.currentSpan();
        span.tag("firstTag", "hello world");
        span.tag("secondTag", "sleuth example");
        restTemplate.getForObject(secondUri + "/ping", String.class);
    }
}
