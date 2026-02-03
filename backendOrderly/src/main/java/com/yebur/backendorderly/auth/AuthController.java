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
                        req.email(),
                        req.password()
                )
        );

        UserDetails user = (UserDetails) auth.getPrincipal();
        return new AuthResponse(jwtService.generateToken(user));
    }
}
