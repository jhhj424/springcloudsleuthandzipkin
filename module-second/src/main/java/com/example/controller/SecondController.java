package com.example.controller;

import com.example.service.SecondService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/second")
@RequiredArgsConstructor
@Slf4j
public class SecondController {

    private final SecondService secondService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info(">>> second-point .... ");
        String result = secondService.ping();
        log.info(">>> second-point .... {} ", result);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/ping")
    public ResponseEntity<String> pingPost() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.info(">>> second-point X-B3-Sampled header : {} ", request.getHeader("X-B3-Sampled"));
        log.info(">>> second-point test header : {} ", request.getHeader("test"));
        String result = secondService.ping();
        log.info(">>> second-point .... {} ", result);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/send-third")
    public ResponseEntity<String> sendThird() {
        log.info(">>> second-point .... ");
        String result = secondService.sendThird();
        log.info(">>> second-point .... {} ", result);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/error")
    private void error() {
        log.info(">>> second point ");
        secondService.createError();
    }

    @GetMapping("/baggage")
    public void baggage() {
        log.info(">>> second-point .... ");
        secondService.findBaggage();
        log.info(">>> second-point end");
    }

}
