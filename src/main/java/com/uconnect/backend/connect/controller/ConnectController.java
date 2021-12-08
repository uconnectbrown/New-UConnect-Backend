package com.uconnect.backend.connect.controller;

import com.uconnect.backend.connect.service.ConnectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ConnectController {

    private final ConnectService connectService;

    @Autowired
    public ConnectController(ConnectService connectService) {
        this.connectService = connectService;
    }

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
                msg = "Operation unsuccessful: " + senderUsername + " has already sent a request to "
                        + receiverUsername;
                break;
            case -2:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + receiverUsername + " has already received a request from "
                        + senderUsername + ". This should not have happened.";
                break;
            case -3:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has an insufficient number of requests";
                break;
            case -4:
                status = HttpStatus.NOT_FOUND;
                msg = "Operation unsuccessful: user not found";
                break;
            case -5:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                msg = "Unexpected error";
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
                msg = "Operation unsuccessful: " + receiverUsername + " has not received a request from "
                        + senderUsername + ". This should not have happened.";
                break;
            case -3:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername
                        + " has too many requests. This should not have happened.";
                break;
            case -4:
                status = HttpStatus.NOT_FOUND;
                msg = "Operation unsuccessful: user not found";
                break;
            case -5:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                msg = "Unexpected error";
                break;
            default:
                status = HttpStatus.I_AM_A_TEAPOT;
                msg = "This should not have happened. A certain dev asks that you call your mother for him.";
        }

        return new ResponseEntity<>(msg, status);
    }

    @PostMapping("v1/connect/accept")
    public ResponseEntity<String> accept(@RequestBody Map<String, String> req) {
        String senderUsername = req.get("sender");
        String receiverUsername = req.get("receiver");

        int result = connectService.accept(senderUsername, receiverUsername);

        HttpStatus status;
        String msg;
        switch (result) {
            case 0:
                status = HttpStatus.OK;
                msg = "Successfully accepted request from " + senderUsername + " to " + receiverUsername;
                break;
            case -1:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + receiverUsername + " has no request from " + senderUsername
                        + ". This should not have happened.";
                break;
            case -2:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " has not sent a request to " + receiverUsername
                        + ". This should not have happened.";
                break;
            case -3:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + receiverUsername + " is already connected with " + senderUsername
                        + ". This should not have happened.";
                break;
            case -4:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername + " is already connected with " + receiverUsername
                        + ". This should not have happened.";
                break;
            case -5:
                status = HttpStatus.BAD_REQUEST;
                msg = "Operation unsuccessful: " + senderUsername
                        + " has too many requests. This should not have happened.";
                break;
            case -6:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                msg = "Unexpected error";
                break;
            default:
                status = HttpStatus.I_AM_A_TEAPOT;
                msg = "This should not have happened. A certain dev asks that you call your mother for him.";
        }

        return new ResponseEntity<>(msg, status);
    }
}
