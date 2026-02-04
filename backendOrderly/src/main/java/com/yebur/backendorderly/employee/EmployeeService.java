package com.yebur.backendorderly.employee;

import com.yebur.backendorderly.role.Role;
import com.yebur.backendorderly.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService implements EmployeeServiceInterface {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAllEmployeesDTO() {
        return employeeRepository.findAll().stream()
                .map(EmployeeResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> findEmployeeDTOById(Long id) {
        return employeeRepository.findById(id)
                .map(EmployeeResponse::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> findEmployeeDTOByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(EmployeeResponse::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployeeResponse> findCurrentEmployeeDTO(Authentication auth) {
        String email = auth.getName();
        return employeeRepository.findByEmail(email)
                .map(EmployeeResponse::mapToResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        Employee employee = new Employee();
        updateEmployeeFields(employee, request);
        employee.setPassword(passwordEncoder.encode(request.getPassword()));

        Employee savedEmployee = employeeRepository.save(employee);
        return EmployeeResponse.mapToResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));

        if (!employee.getEmail().equals(request.getEmail()) && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        updateEmployeeFields(employee, request);
        
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        return EmployeeResponse.mapToResponse(updatedEmployee);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    private void updateEmployeeFields(Employee employee, EmployeeRequest request) {
        employee.setName(request.getName());
        employee.setLastname(request.getLastname());
        employee.setEmail(request.getEmail());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setHireDate(request.getHireDate());
        employee.setStatus(request.getStatus() != null ? request.getStatus() : EmployeeStatus.ACTIVE);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoles()));
            employee.setRoles(roles);
        } else {
            employee.setRoles(new HashSet<>());
        }
    }
}
