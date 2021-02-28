package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.payload.request.LoginRequest;
import nl.bvsit.coworker.payload.request.SignupRequest;
import nl.bvsit.coworker.payload.response.JwtResponse;
import nl.bvsit.coworker.payload.response.MessageResponse;
import nl.bvsit.coworker.service.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthorizationService authorizationService;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody LoginRequest loginRequest) {
        return authorizationService.authenticateUser(loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(@RequestBody SignupRequest signUpRequest) {
        return authorizationService.registerCoworker(signUpRequest);
    }

    @PostMapping("/signup/employee")
    public ResponseEntity<MessageResponse> registerEmployee(@RequestBody SignupRequest signUpRequest) {
        return authorizationService.registerEmployee(signUpRequest);
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<MessageResponse> registerAdmin(@RequestBody SignupRequest signUpRequest) {
        return authorizationService.registerAdmin(signUpRequest);
    }

    @PostMapping("/signup/manager")
    public ResponseEntity<MessageResponse> registerManager(@RequestBody SignupRequest signUpRequest) {
        return authorizationService.registerManager(signUpRequest);
    }
}
