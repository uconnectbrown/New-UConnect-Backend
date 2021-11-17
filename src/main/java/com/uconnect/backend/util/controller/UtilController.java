package com.uconnect.backend.util.controller;

import java.util.List;

import com.uconnect.backend.util.service.UtilService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UtilController {
    
    @Autowired
    private UtilService utilService;

    @GetMapping("/v1/util/getEmails")
    public ResponseEntity<List<String>> getEmails() {
        return new ResponseEntity<>(utilService.getEmails(), HttpStatus.OK);
    }
}
