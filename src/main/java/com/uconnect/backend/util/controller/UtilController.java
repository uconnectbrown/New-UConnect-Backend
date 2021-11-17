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

    @GetMapping("/v1/util/getAllEmails")
    public ResponseEntity<List<String>> getAllEmails() {
        return new ResponseEntity<>(utilService.getAllEmails(), HttpStatus.OK);
    }

    @GetMapping("/v1/util/getAllPending")
    public ResponseEntity<List<String>> getAllPending() {
        return new ResponseEntity<>(utilService.getAllPending(), HttpStatus.OK);
    }
}
