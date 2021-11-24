package com.uconnect.backend.connect.controller;

import java.util.Map;

import com.uconnect.backend.connect.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectController {
    
    @Autowired
    private ConnectService connectService;

    @PostMapping("/v1/connect/request")
    public ResponseEntity<String> request(@RequestBody Map<String, String> req) {
        String senderUsername = req.get("sender");
        String receiverUsername = req.get("receiver");

        int result = connectService.request(senderUsername, receiverUsername);

        HttpStatus status;
        String msg;
        switch (result) {
            case 0:
                status = HttpStatus.OK;
                msg = "Successfully created a request from " + senderUsername + " to " + receiverUsername;
                break;
            case -1:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has already sent a request to " + receiverUsername;
                break;
            case -2:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + receiverUsername + " has already received a request from " + receiverUsername + ". This should not have happened.";
                break;
            case -3:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has an insufficient number of requests";
                break;
            default:
                status = HttpStatus.I_AM_A_TEAPOT;
                msg = "This should not have happened. A certain dev asks that you call your mother for him.";
        }

        return new ResponseEntity<>(msg, status);
    }

    @PostMapping("/v1/connect/undoRequest")
    public ResponseEntity<String> undoRequest(@RequestBody Map<String, String> req) {
        String senderUsername = req.get("sender");
        String receiverUsername = req.get("receiver");

        int result = connectService.undoRequest(senderUsername, receiverUsername);

        HttpStatus status;
        String msg;
        switch (result) {
            case 0:
                status = HttpStatus.OK;
                msg = "Successfully created a request from " + senderUsername + " to " + receiverUsername;
                break;
            case -1:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has not sent a request to " + receiverUsername;
                break;
            case -2:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + receiverUsername + " has not received a request from " + receiverUsername + ". This should not have happened.";
                break;
            case -3:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has too many requests. This should not have happened.";
                break;
            default:
                status = HttpStatus.I_AM_A_TEAPOT;
                msg = "This should not have happened. A certain dev asks that you call your mother for him.";
        }

        return new ResponseEntity<>(msg, status);
    }
}
