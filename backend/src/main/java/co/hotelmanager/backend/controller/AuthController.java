package co.hotelmanager.backend.controller;

import co.hotelmanager.backend.dto.request.LoginRequest;
import co.hotelmanager.backend.dto.request.RegisterRequest;
import co.hotelmanager.backend.dto.response.JwtResponse;
import co.hotelmanager.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login — público, no requiere token
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/register — solo admin (protegido en SecurityConfig)
    @PostMapping("/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.status(201).build();
    }
}