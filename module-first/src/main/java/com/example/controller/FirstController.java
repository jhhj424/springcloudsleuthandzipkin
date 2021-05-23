package com.example.controller;

import com.example.service.FirstService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/first")
@RequiredArgsConstructor
@Slf4j
public class FirstController {

    private final FirstService firstService;

    final static private String REQUEST_SUCCESS = "request success!";

    @GetMapping("/start")
    public ResponseEntity<String> start() {
        log.info(">>> start .. first controller ... ");
        log.info(">>> first controller ... {}", firstService.sendSecond());
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

    @GetMapping("/new")
    public ResponseEntity<String> newTracer() {
        log.info(">>> start .. first controller ... ");
        log.info(">>> first controller ... {}", firstService.createNewTracer());
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

    @GetMapping("/send-third")
    public ResponseEntity<String> sendThird() {
        log.info(">>> start .. first controller ... ");
        log.info(">>> first controller ... {}", firstService.sendSecondAndThird());
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

    @GetMapping("/error")
    public ResponseEntity<String> error() {
        log.info(">>> first point ");
        firstService.sendSecondForErrorTest();
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

    @GetMapping("/add_tag")
    public ResponseEntity<String> addTag() {
        firstService.addTag("annotation tag");
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

    @GetMapping("/add_span")
    public ResponseEntity<String> addSpan(@RequestParam String spanType) {
        log.info(">>> addSpan request start ... ");
        if (spanType.equals("newTrace")){
            firstService.addSpan();
        } else if (spanType.equals("nextSpan")){
            firstService.nextSpan();
        }
        log.info(">>> addSpan request end ... ");
        return ResponseEntity.ok().body(REQUEST_SUCCESS);
    }

}
