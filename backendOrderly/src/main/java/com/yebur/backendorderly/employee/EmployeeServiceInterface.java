package com.yebur.backendorderly.employee;

import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface EmployeeServiceInterface {
    List<EmployeeResponse> findAllEmployeesDTO();
    Optional<EmployeeResponse> findEmployeeDTOById(Long id);
    Optional<EmployeeResponse> findEmployeeDTOByEmail(String email);
    Optional<EmployeeResponse> findCurrentEmployeeDTO(Authentication auth);
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
}
