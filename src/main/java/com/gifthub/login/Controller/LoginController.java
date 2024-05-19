package com.gifthub.login.Controller;

//import com.gifthub.login.Jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RequiredArgsConstructor
@RestController
public class LoginController {

//    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/main")
    public String index() {
        return "this is main page";
    }

    @GetMapping("/login/google")
    public ResponseEntity<?> GoogleLogin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/oauth2/authorization/google"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @GetMapping("/login/apple")
    public ResponseEntity<?> AppleLogin() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/oauth2/authorization/apple"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

}
