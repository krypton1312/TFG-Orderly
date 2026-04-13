package com.yebur.backendorderly.employee;

import com.yebur.backendorderly.role.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String name;
    private String lastname;
    private List<RoleResponse> roles;
    private String phoneNumber;
    private String email;
    private String username;
    private LocalDate hireDate;
    private String status;
    /** Only populated on create / reset-password. Null in all other responses. */
    private String tempPassword;

    public static EmployeeResponse mapToResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getLastname(),
                employee.getRoles() != null ? employee.getRoles().stream()
                        .map(RoleResponse::mapToResponse)
                        .collect(Collectors.toList()) : null,
                employee.getPhoneNumber(),
                employee.getEmail(),
                employee.getUsername(),
                employee.getHireDate(),
                employee.getStatus() != null ? employee.getStatus().getSpanishName() : null,
                null
        );
    }
}
