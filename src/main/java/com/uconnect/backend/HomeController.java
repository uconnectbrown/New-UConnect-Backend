package com.uconnect.backend;

import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "You are logged in, John Cena!";
    }

    @PostMapping("/api/authenticate")
    public JwtResponse authenticate(@RequestBody JwtRequest jwtRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    jwtRequest.getUsername(),
                    jwtRequest.getPassword()
            ));
        } catch (Exception e) {
            throw new Exception("INVALID CREDENTIALS", e);
        }

        final User user = userService.loadUserByUsername(jwtRequest.getUsername());

        final String token = jwtUtility.generateToken(user);

        return new JwtResponse(token);
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
