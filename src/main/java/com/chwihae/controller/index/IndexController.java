package com.chwihae.controller.index;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    @GetMapping
    public ResponseEntity<String> index() {
        return new ResponseEntity<>("index", HttpStatus.OK);
    }
}
