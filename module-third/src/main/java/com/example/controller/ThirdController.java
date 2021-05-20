package com.example.controller;

import com.example.service.ThirdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/third")
@RequiredArgsConstructor
@Slf4j
public class ThirdController {

    private final ThirdService thirdService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        log.info(">>> third-point .... ");
        String result = thirdService.ping();
        log.info(">>> third-point .... {} ", result);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/ping")
    public ResponseEntity<String> pingPost() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.info(">>> third-point X-B3-Sampled header : {} ", request.getHeader("X-B3-Sampled"));
        log.info(">>> third-point test header : {} ", request.getHeader("test"));
        String result = thirdService.ping();
        log.info(">>> third-point .... {} ", result);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/error")
    private void error() {
        log.info(">>> third point ");
        thirdService.createError();
    }

    @GetMapping("/baggage")
    public void baggage() {
        log.info(">>> third-point .... ");
        thirdService.findBaggage();
        log.info(">>> third-point end");
    }

}
