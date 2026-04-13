package com.yebur.backendorderly.auth;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.yebur.backendorderly.auth.dto.*;
import com.yebur.backendorderly.employee.*;
import com.yebur.backendorderly.role.Role;
import com.yebur.backendorderly.role.RoleRepository;
import com.yebur.backendorderly.security.CustomUserDetailsService;
import com.yebur.backendorderly.security.jwt.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterRequest req
    ) {
        if (employeeRepository.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().build();
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("ROLE USER not found"));

        Employee e = new Employee();
        e.setName(req.name());
        e.setLastname(req.lastname());
        e.setEmail(req.email());
        e.setUsername(generateUsername(req.name(), req.lastname()));
        e.setPassword(passwordEncoder.encode(req.password()));
        e.setHireDate(LocalDate.now());
        e.setStatus(EmployeeStatus.ACTIVE);
        e.setRoles(Set.of(userRole));

        employeeRepository.save(e);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody @Valid LoginRequest req
    ) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.identifier(),
                        req.password()
                )
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    private String generateUsername(String name, String lastname) {
        String base = (String.valueOf(name.charAt(0)) + lastname)
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        if (!employeeRepository.existsByUsername(base)) return base;
        int i = 1;
        while (employeeRepository.existsByUsername(base + i)) i++;
        return base + i;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        String email;
        try {
            email = jwtService.extractEmail(req.refreshToken());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetails user = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isValid(req.refreshToken(), user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String newAccessToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(newAccessToken, req.refreshToken()));
    }
}
