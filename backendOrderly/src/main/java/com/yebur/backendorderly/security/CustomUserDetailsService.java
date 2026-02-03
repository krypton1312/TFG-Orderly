package com.yebur.backendorderly.security;

import com.yebur.backendorderly.employee.Employee;
import com.yebur.backendorderly.employee.EmployeeRepository;
import com.yebur.backendorderly.employee.EmployeeStatus;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Employee e = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Employee not found"));

        var authorities = e.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .toList();

        boolean enabled = e.getStatus() == EmployeeStatus.ACTIVE;

        return User.builder()
                .username(e.getEmail())
                .password(e.getPassword())
                .authorities(authorities)
                .disabled(!enabled)
                .build();
    }
}
