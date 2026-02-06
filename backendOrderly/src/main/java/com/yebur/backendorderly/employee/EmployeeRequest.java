package com.yebur.backendorderly.employee;

import java.time.LocalDate;
import java.util.Set;

import com.yebur.backendorderly.role.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    private String name;

    private String lastname;

    private Set<Long> roles;

    private String phoneNumber;

    @Email(message = "Email should be valid")
    private String email;

    private LocalDate hireDate;

    private String password;

    private EmployeeStatus status;
}
