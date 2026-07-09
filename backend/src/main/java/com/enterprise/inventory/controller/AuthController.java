package com.enterprise.inventory.controller;

import com.enterprise.inventory.dto.AuthResponse;
import com.enterprise.inventory.dto.CurrentUserResponse;
import com.enterprise.inventory.dto.LoginRequest;
import com.enterprise.inventory.entity.AppUser;
import com.enterprise.inventory.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                );

        AppUser user = (AppUser) authenticationManager.authenticate(authToken).getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Principal principal) {
        AppUser user = (AppUser) ((org.springframework.security.authentication.UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return CurrentUserResponse.from(user);
    }
}
