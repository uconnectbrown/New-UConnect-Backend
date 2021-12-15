package com.uconnect.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "You are logged in, John Cena!";
    }

    @GetMapping("/testReq")
    public String testReq(@RequestParam String who) throws Exception {
        return "Hello, " + who;
    }

    @GetMapping("/testPath/{who}")
    public String testPath(@PathVariable String who) throws Exception {
        return "Hello, " + who;
    }

    @GetMapping("/testThree/{who}")
    public String testThree(@PathVariable("who") String who) throws Exception {
        return "Hello, " + who;
    }
}
